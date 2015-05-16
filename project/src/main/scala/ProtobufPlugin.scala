package src.main.scala

import java.io.File

import sbt.Keys._
import sbt._
import src.main.scala.Jni.Keys._

import scala.language.postfixOps

object ProtobufPlugin extends Plugin {

  val Protobuf = config("protobuf")

  object Keys {
    val generate = taskKey[Seq[File]]("Compile the protobuf sources.")

    val protoc = settingKey[File]("The path+name of the protoc executable.")
    val generatedTargets = settingKey[Seq[(File, String)]]("Targets for protoc: target directory and glob for generated source files")

    val scalabuffVersion =  SettingKey[String]("ScalaBuff version.")
  }

  import Keys._

  override val settings = inConfig(Protobuf)(Seq[Setting[_]](
    sourceDirectory := (sourceDirectory in Compile).value / "protobuf",
    javaSource := (sourceManaged in Compile).value / "compiled_protobuf",

    protoc := file("protoc"),
    version := {
      object versionLog extends ProcessLogger {
        import scala.collection.mutable

        val result = new mutable.StringBuilder

        def buffer[T](f: => T): T = f
        def error(s: => String) = {
          sys.error("unexpected error output from protoc: " + s)
        }
        def info(s: => String) = {
          result ++= s
        }
      }
      Seq(protoc.value.getPath, "--version") !< versionLog

      versionLog.result.mkString.split(" ")(1).trim
    },

    scalabuffVersion := "1.4.0",

    managedClasspath <<= (classpathTypes, update) map { (ct, report) =>
      Classpaths.managedJars(Protobuf, ct, report)
    },

    generate <<= (
      streams,
      managedClasspath,
      javaHome,
      javaSource,
      managedNativeSource in Compile,
      sourceDirectory,
      protoc
    ) map sourceGeneratorTask

  )) ++ Seq[Setting[_]](
    libraryDependencies <++= (scalabuffVersion in Protobuf)(version =>
      Seq(
        "net.sandrogrzicic" %% "scalabuff-compiler" % version % Protobuf.name,
        "net.sandrogrzicic" %% "scalabuff-runtime" % version
      )
    ),
    libraryDependencies <+= (version in Protobuf)("com.google.protobuf" % "protobuf-java" % _),

    sourceGenerators in Compile <+= generate in Protobuf,
    managedSourceDirectories in Compile += (javaSource in Protobuf).value,
    jniSourceFiles in Compile ++= ((managedNativeSource in Compile).value ** "*.pb.cpp").get
  )

  private def sourceGeneratorTask(
    streams: TaskStreams,
    managedClasspath: Classpath,
    javaHome: Option[File],
    javaSource: File,
    managedNativeSource: File,
    sourceDirectory: File,
    protoc: File
  ) = {
    val schemas = (sourceDirectory ** "*.proto").get.map(_.getAbsoluteFile).toSet
    val cachedCompile =
      FileFunction.cached(
        streams.cacheDirectory / "protobuf",
        inStyle = FilesInfo.lastModified,
        outStyle = FilesInfo.exists
      ) { (in: Set[File]) =>
        schemas.foreach(schema => streams.log.info(s"Compiling schema $schema"))

        // Compile to Scala sources.
        val scalaBuffOutputs = compileScalaBuff(in, managedClasspath, javaHome, javaSource, sourceDirectory, streams.log)

        // Compile to C++ and Java sources.
        val protocOutputs = compileProtoc(protoc, in, javaSource, managedNativeSource, sourceDirectory, streams.log)

        (scalaBuffOutputs ++ protocOutputs).toSet
      }
    cachedCompile(schemas).toSeq
  }

  private def compileScalaBuff(
    schemas: Set[File],
    managedClasspath: Classpath,
    javaHome: Option[File],
    javaSource: File,
    sourceDirectory: File,
    log: Logger
  ) = {
    val scalaOut = javaSource

    scalaOut.mkdirs()

    val arguments = Seq(
      "-cp", managedClasspath.map(_.data).mkString(File.pathSeparator),
      "net.sandrogrzicic.scalabuff.compiler.ScalaBuff",
      "--scala_out=" + scalaOut.getAbsolutePath
    ) ++ schemas.toSeq.map(_.toString)

    log.debug("scalabuff options:")
    arguments.map("\t" + _).foreach(log.debug(_))

//    val exitCode = Fork.java(ForkOptions(javaHome), arguments)
//    if (exitCode != 0) {
//      throw new RuntimeException(s"scalabuff-compiler returned exit code: $exitCode")
//    }

    (scalaOut ** "*.scala").get
  }

  private def compileProtoc(
    protoc: File,
    schemas: Set[File],
    javaSource: File,
    managedNativeSource: File,
    sourceDirectory: File,
    log: Logger
  ) = {
    val javaOut = javaSource
    val cppOut = managedNativeSource

    javaOut.mkdirs()
    cppOut.mkdirs()

    val protocOptions = Seq(
      s"--java_out=${javaOut.absolutePath}",
      s"--cpp_out=${cppOut.absolutePath}"
    )

    log.debug("protoc options:")
    protocOptions.map("\t" + _).foreach(log.debug(_))

    val exitCode =
      try {
        val command = Seq(protoc.getPath) ++ Seq("-I" + sourceDirectory.absolutePath) ++ protocOptions ++ schemas.map(_.absolutePath)
        command ! log
      } catch {
        case e: Exception =>
          throw new RuntimeException(s"error occured while compiling protobuf files: ${e.getMessage}", e)
      }
    if (exitCode != 0) {
      throw new RuntimeException(s"protoc returned exit code: $exitCode")
    }

    (javaOut ** "*.java").get
  }

}
