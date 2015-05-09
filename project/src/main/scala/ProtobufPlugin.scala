import Jni.Keys._
import sbt.Keys._
import sbt._

import scala.language.postfixOps

object ProtobufPlugin extends Plugin {
  val protobufConfig = config("protobuf")

  object Keys {
    val includePaths = taskKey[Seq[File]]("The paths that contain *.proto dependencies.")
    val generate = taskKey[Seq[File]]("Compile the protobuf sources.")
    val unpackDependencies = taskKey[UnpackedDependencies]("Unpack dependencies.")

    val protoc = settingKey[Option[File]]("The path of the protoc executable.")
    val externalIncludePath = settingKey[File]("The path to which protobuf:library-dependencies are extracted and which is used as protobuf:include-path for protoc")
    val generatedTargets = settingKey[Seq[(File,String)]]("Targets for protoc: target directory and glob for generated source files")
    val protocOptions = settingKey[Seq[String]]("Additional options to be passed to protoc")
  }

  import ProtobufPlugin.Keys._

  private val osName =
    sys.props("os.name") match {
      case "Linux"    => "linux"
      case "Mac OS X" => "osx"
      case "Windows"  => "windows"
      case name =>
        sys.error("Unsupported OS: " + name)
    }

  override val settings: Seq[Setting[_]] = inConfig(protobufConfig)(Seq[Setting[_]](

    sourceDirectory <<= (sourceDirectory in Compile) { _ / "protobuf" },
    sourceDirectories <<= sourceDirectory apply (_ :: Nil),
    javaSource <<= (sourceManaged in Compile) { _ / "compiled_protobuf" },
    externalIncludePath <<= target(_ / "protobuf_external"),
    protoc := None,
    version := "2.6.1",

    generatedTargets := Nil,
    generatedTargets <+= javaSource((_, "*.java")), // add javaSource to the list of patterns
    generatedTargets <+= (managedNativeSource in Compile)((_, "*.pb.cpp")),

    protocOptions := Nil,
    protocOptions <++= generatedTargets { generatedTargets =>
      val java_out =
        generatedTargets.find(_._2.endsWith(".java")) match {
          case Some(targetForJava) => Seq("--java_out=%s".format(targetForJava._1.absolutePath))
          case None => Nil
        }
      val cpp_out =
        generatedTargets.find(_._2.endsWith(".pb.cpp")) match {
          case Some(targetForCpp) => Seq("--cpp_out=%s".format(targetForCpp._1.absolutePath))
          case None => Nil
        }
      java_out ++ cpp_out
    },

    managedClasspath <<= (classpathTypes, update) map { (ct, report) =>
      Classpaths.managedJars(protobufConfig, ct, report)
    },

    unpackDependencies <<= unpackDependenciesTask,

    includePaths <<= sourceDirectory map (_ :: Nil),
    includePaths <+= externalIncludePath map identity,

    generate <<= sourceGeneratorTask dependsOn unpackDependencies

  )) ++ Seq[Setting[_]](
    sourceGenerators in Compile <+= generate in protobufConfig,
    cleanFiles <++= (generatedTargets in protobufConfig) { _.map(_._1) },
    cleanFiles <+= (externalIncludePath in protobufConfig),
    managedSourceDirectories in Compile <++= (generatedTargets in protobufConfig) { _.map(_._1) },
    libraryDependencies <+= (version in protobufConfig)("com.google.protobuf" % "protobuf-java" % _),
    libraryDependencies <+= (version in protobufConfig)("com.google.protobuf" % "protoc" % _ artifacts(Artifact("protoc", "exe", "exe", osName + "-" + sys.props("os.arch")))),
    ivyConfigurations += protobufConfig,

    jniSourceFiles in Compile <++= (generatedTargets in protobufConfig) { generatedTargets =>
      generatedTargets.find(_._2.endsWith(".pb.cpp")) match {
        case Some(targetForCpp) => (targetForCpp._1 ** targetForCpp._2).get
        case None => Nil
      }
    }
  )

  case class UnpackedDependencies(dir: File, files: Seq[File])

  private def executeProtoc(protoc: File, schemas: Set[File], includePaths: Seq[File], protocOptions: Seq[String], log: Logger) =
    try {
      val incPath = includePaths.map("-I" + _.absolutePath)
      val command = Seq(protoc.getAbsolutePath) ++ incPath ++ protocOptions ++ schemas.map(_.absolutePath)
      command ! log
    } catch { case e: Exception =>
      throw new RuntimeException("error occured while compiling protobuf files: %s" format(e.getMessage), e)
    }


  private def compile(protoc: File, schemas: Set[File], includePaths: Seq[File], protocOptions: Seq[String], generatedTargets: Seq[(File, String)], log: Logger) = {
    val generatedTargetDirs = generatedTargets.map(_._1)

    generatedTargetDirs.foreach(_.mkdirs())

    log.info("Compiling %d protobuf files to %s".format(schemas.size, generatedTargetDirs.mkString(",")))
    log.debug("protoc options:")
    protocOptions.map("\t" + _).foreach(log.debug(_))
    schemas.foreach(schema => log.info("Compiling schema %s" format schema))

    val exitCode = executeProtoc(protoc, schemas, includePaths, protocOptions, log)
    if (exitCode != 0)
      sys.error("protoc returned exit code: %d" format exitCode)

    (generatedTargets.flatMap{ot => (ot._1 ** ot._2).get}).toSet
  }

  private def unpack(deps: Seq[File], externalIncludePath: File, log: Logger): Seq[File] = {
    IO.createDirectory(externalIncludePath)
    deps.flatMap { dep =>
      val seq = IO.unzip(dep, externalIncludePath, "*.proto").toSeq
      if (!seq.isEmpty) log.debug("Extracted " + seq.mkString("\n * ", "\n * ", ""))
      seq
    }
  }

  private def sourceGeneratorTask =
    (streams, sourceDirectories in protobufConfig, includePaths in protobufConfig, protocOptions in protobufConfig, generatedTargets in protobufConfig, protoc, version, ivyPaths) map {
      (streams, sourceDirectories, includePaths, protocOptions, generatedTargets, protoc, version, ivyPaths) =>
        val protocExe =
          protoc getOrElse {
            ivyPaths.ivyHome match {
              case None => sys.error("No protoc set and no ivyPaths.ivyHome set")
              case Some(ivyHome) =>
                ivyHome / "cache" / "com.google.protobuf" / "protoc" / "exes" / ("protoc-" + version + "-" + osName + "-" + sys.props("os.arch") + ".exe")
            }
          }
        if (!protocExe.canExecute) {
          protocExe.setExecutable(true)
        }

        val schemas = sourceDirectories.toSet[File].flatMap(srcDir => (srcDir ** "*.proto").get.map(_.getAbsoluteFile))
        val cachedCompile =
          FileFunction.cached(
            streams.cacheDirectory / "protobuf",
            inStyle = FilesInfo.lastModified,
            outStyle = FilesInfo.exists
          ) { in =>
            compile(protocExe, schemas, includePaths, protocOptions, generatedTargets, streams.log)
          }
        cachedCompile(schemas).toSeq
    }

  private def unpackDependenciesTask =
    (streams, managedClasspath in protobufConfig, externalIncludePath in protobufConfig) map {
      (streams, managedClasspath, externalIncludePath) =>
        val extractedFiles = unpack(managedClasspath.map(_.data), externalIncludePath, streams.log)
        UnpackedDependencies(externalIncludePath, extractedFiles)
    }
}
