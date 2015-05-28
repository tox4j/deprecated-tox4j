package sbt.tox4j

import java.io.File

import net.sandrogrzicic.scalabuff.compiler.ScalaBuff
import sbt.Keys._
import sbt._
import sbt.tox4j.Jni.Keys._

import scala.language.postfixOps

object ProtobufPlugin extends Plugin {

  val Protobuf = config("protobuf")

  object Keys {
    val generate = taskKey[Seq[File]]("Compile the protobuf sources.")

    val protoc = settingKey[File]("The path+name of the protoc executable.")
    val generatedTargets = settingKey[Seq[(File, String)]]("Targets for protoc: target directory and glob for generated source files")

    val scalabuffVersion = SettingKey[String]("ScalaBuff version.")
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
      javaSource,
      managedNativeSource in Compile,
      sourceDirectory,
      protoc
    ) map sourceGeneratorTask

  )) ++ Seq[Setting[_]](
    libraryDependencies <+= (scalabuffVersion in Protobuf)(version => "net.sandrogrzicic" %% "scalabuff-runtime" % version),
    libraryDependencies <+= (version in Protobuf)("com.google.protobuf" % "protobuf-java" % _),

    sourceGenerators in Compile <+= generate in Protobuf,
    managedSourceDirectories in Compile += (javaSource in Protobuf).value,
    jniSourceFiles in Compile ++= ((managedNativeSource in Compile).value ** "*.pb.cpp").get
  )

  private def sourceGeneratorTask(
    streams: TaskStreams,
    javaSource: File,
    managedNativeSource: File,
    sourceDirectory: File,
    protoc: File
  ) = {
    val cached =
      FileFunction.cached(
        streams.cacheDirectory / "protobuf",
        inStyle = FilesInfo.lastModified,
        outStyle = FilesInfo.exists
      ) { (in: Set[File]) =>
        // Compile to C++ sources.
        compileProtoc(protoc, in, managedNativeSource, sourceDirectory, streams.log)

        // Compile to Scala sources.
        compileScalaBuff(in, javaSource, streams.log).toSet
      }

    val schemas = (sourceDirectory ** "*.proto").get.map(_.getAbsoluteFile)
    schemas.foreach(schema => streams.log.info(s"Compiling schema $schema"))

    cached(schemas.toSet).toSeq
  }

  private def compileScalaBuff(
    schemas: Set[File],
    javaSource: File,
    log: Logger
  ) = {
    val scalaOut = javaSource
    scalaOut.mkdirs()

    val settings = ScalaBuff.Settings(
      outputDirectory = scalaOut,
      generateJsonMethod = true
    )
    if (!ScalaBuff.run(settings, schemas)) {
      sys.error(s"scalabuff-compiler failed")
    }

    (scalaOut ** "*.scala").get
  }

  private def compileProtoc(
    protoc: File,
    schemas: Set[File],
    managedNativeSource: File,
    sourceDirectory: File,
    log: Logger
  ): Unit = {
    val cppOut = managedNativeSource
    cppOut.mkdirs()

    val protocOptions = Seq(s"--cpp_out=${cppOut.absolutePath}")

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
      sys.error(s"protoc returned exit code: $exitCode")
    }
  }

}
