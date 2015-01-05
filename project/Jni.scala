import sbt._
import Keys._

import scala.language.postfixOps

object Jni extends Plugin {
  object Keys {

    // settings

    val useCMake = settingKey[Boolean]("Use CMake instead of built-in C++ build system")

    val libraryName = settingKey[String]("Shared library produced by JNI")

    val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")
    val versionSync = settingKey[String]("Package from pkg-config we want to sync our version number with")

    val nativeSource = settingKey[File]("JNI native sources")
    val nativeTarget = settingKey[File]("JNI native target directory")
    val managedNativeSource = settingKey[File]("Generated JNI native sources")

    val binPath = settingKey[File]("Shared libraries produced by JNI")

    val nativeCC = settingKey[String]("Compiler to use")
    val nativeCXX = settingKey[String]("Compiler to use")

    val ccOptions = settingKey[Seq[String]]("Flags to be passed to the native compiler when compiling")
    val ldOptions = settingKey[Seq[String]]("Flags to be passed to the native compiler when linking")

    val jniClasses = settingKey[Seq[String]]("Classes with native methods")
    val jniSourceFiles = settingKey[Seq[File]]("JNI source files")

  }

  import Keys._


  private val jniConfig = config("native")

  private object PrivateKeys {

    // tasks

    val checkVersion = taskKey[Unit]("Check the versionSync variable")
    val javah = taskKey[Unit]("Generates JNI header files")
    val jniCompile = taskKey[Unit]("Compiles JNI native sources")

    // settings

    val headersPath = settingKey[File]("Generated JNI headers")
    val includes = settingKey[Seq[File]]("Compiler include directories")

  }

  import PrivateKeys._


  private object nullLog extends AnyRef with ProcessLogger {
    def buffer[T](f: => T): T = f
    def error(s: => String) = { }
    def info(s: => String) = { }
  }

  /**
   * Extensions of source files.
   */
  private val cppExtensions = Seq(".cpp", ".cc", ".cxx", ".c")

  private def filterNativeSources(files: Seq[File]) = {
    files.filter { file =>
      file.isFile && cppExtensions.exists(file.getName.toLowerCase.endsWith)
    }
  }

  private val jdkHome = {
    val home = file(sys.props("java.home"))
    if (home.exists)
      Some(home.getAbsoluteFile)
    else
      None
  }

  private val jreInclude = {
    jdkHome.map { home =>
      val absHome = home.getParentFile
      // In a typical installation, JDK files are one directory above the
      // location of the JRE set in 'java.home'.
      Seq(absHome / "include")
    }
  }

  private def pkgConfig(query: String, pkgs: Seq[String]) = {
    pkgs map { pkg =>
      (pkg, Seq("pkg-config", pkg) !< nullLog != 0)
    } filter (_._2) map (_._1) match {
      case Nil =>
      case missing =>
        sys.error(s"missing ${missing.size} packages: ${missing.mkString(", ")}")
    }

    pkgs match {
      case Nil =>
        Nil
      case _ =>
        val command = Seq("pkg-config", "--" + query) ++ pkgs
        (command !!).split(" ").map(_.trim).filter(!_.isEmpty).toSeq
    }
  }

  private def pkgConfig(pkg: String): String = {
    pkgConfig("modversion", Seq(pkg)).head
  }


  private val toolchain = Option(System.getenv("TOOLCHAIN")) map file map (_.getAbsoluteFile)
  private def mkToolchain(tools: Seq[String]) = {
    val prefixTools =
      toolchain map { toolchain =>
        val triple = toolchain.getName
        tools map { tool => (toolchain / "bin" / s"$triple-$tool").getPath }
      } getOrElse Nil
    prefixTools ++ tools
  }

  private def findTool(candidates: String*) = {
    mkToolchain(candidates) find { cc =>
      try {
        Seq(cc, "--version") !< nullLog == 0
      } catch {
        case _: java.io.IOException => false
      }
    } getOrElse "false"
  }

  private def findCc() = findTool("clang", "gcc", "cc")
  private def findCxx() = findTool("clang++", "g++", "c++")

  private def checkCcOptions(compiler: String, code: String, flags: Seq[String]*) = {
    import java.io.File
    import java.io.PrintWriter

    val sourceFile = File.createTempFile("configtest", cppExtensions(0))
    val targetFile = File.createTempFile("configtest", ".out")

    try {
      val out = new PrintWriter(sourceFile)
      try {
        out.println(code)
        out.println("int main () { return 0; }")
      } finally {
        out.close()
      }

      flags find { flags =>
        Seq(compiler, sourceFile.getPath, "-o", targetFile.getPath) ++ flags !< nullLog match {
          case 0 => true
          case _ => false
        }
      } getOrElse Nil
    } finally {
      targetFile.delete()
      sourceFile.delete()
    }
  }

  private def checkExitCode(command: ProcessBuilder, log: Logger) = {
    command ! log match {
      case 0 =>
      case exitCode =>
        sys.error(s"command failed with exit code $exitCode:\n  $command")
    }
  }


  override val settings = inConfig(jniConfig)(Seq(

    // Target for javah-generated headers.
    headersPath := nativeTarget.value / "include",

    // Include directories.
    includes := Nil,

    includes ++= Seq(
      headersPath.value,
      (nativeSource in Compile).value,
      (managedNativeSource in Compile).value
    ),

    includes ++= jreInclude.getOrElse(Nil),

    // Check modversion
    checkVersion := Def.task {
      val log = streams.value.log

      versionSync.value match {
        case "" =>
        case pkg =>
          val pkgVersion = pkgConfig(pkg)
          if (version.value != pkgVersion && version.value != pkgVersion + "-SNAPSHOT") {
            log.warn(s"${name.value} version ${version.value} does not match $pkg version $pkgVersion")
          }
      }
    }.value

  )) ++ Seq(

    // Library name defaults to the project name.
    libraryName := name.value,

    // Initialise pkg-config dependencies to the empty sequence.
    packageDependencies := Nil,
    versionSync := "",

    // Native source directory defaults to "src/main/cpp".
    nativeSource := (sourceDirectory in Compile).value / "cpp",
    nativeTarget := (target in Compile).value / "cpp",
    managedNativeSource := nativeTarget.value / "source",

    // Put the linked library in here.
    binPath := nativeTarget.value / "bin",

    // Default native C++ compiler to Clang.
    nativeCC := findCc(),
    nativeCXX := findCxx(),

    // Use CMake by default.
    useCMake := true,

    // Empty sequences by default.
    ccOptions := Nil,
    ldOptions := Nil,

    // Shared library flags.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fPIC")),
    ldOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-shared")),

    // Check for some C++11 support.
    ccOptions ++= checkCcOptions(nativeCXX.value, "auto f = []{};",
      Seq("-std=c++11"),
      Seq("-std=c++0x")
    ),

    // Debug flags.
    ccOptions ++= checkCcOptions(nativeCXX.value, "",
      Seq("-ggdb3"),
      Seq("-g3"),
      Seq("-g")
    ),

    // Warning flags.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-Wall")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-Wextra")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-pedantic")),

    // No RTTI and no exceptions.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fno-exceptions")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fno-rtti")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-DGOOGLE_PROTOBUF_NO_RTTI")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-DGTEST_HAS_RTTI=0")),

    // Error on undefined references in shared object.
    ldOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-Wl,-z,defs")),

    // Include directories.
    ccOptions ++= (includes in jniConfig).value.map("-I" + _),
    // pkg-config flags.
    ccOptions ++= {
      if (useCMake.value) {
        Nil
      } else {
        pkgConfig("cflags", packageDependencies.value)
      }
    },
    ldOptions ++= {
      if (useCMake.value) {
        Nil
      } else {
        pkgConfig("libs", packageDependencies.value)
      }
    },

    jniSourceFiles := filterNativeSources((nativeSource.value ** "*").get),


    javah := Def.task {
      import java.io.File

      val log = streams.value.log

      val classpath = (
        (dependencyClasspath in Compile).value.files ++
        Seq((classDirectory in Compile).value)
      ).mkString(File.pathSeparator)

      val command = Seq(
        "javah",
        "-d", (headersPath in jniConfig).value.getPath,
        "-classpath", classpath
      ) ++ jniClasses.value

      log.info(s"Running javah to generate ${jniClasses.value.size} JNI headers")
      checkExitCode(command, log)
    }.dependsOn(compile in Compile)
     .dependsOn(checkVersion in jniConfig)
     .tag(Tags.Compile, Tags.CPU)
     .value,


    jniCompile := Def.task {
      val log = streams.value.log

      // Make sure the output directory exists.
      binPath.value.mkdirs()

      val cxxflags = ccOptions.value.distinct
      val ldflags = ldOptions.value.distinct
      val sources = jniSourceFiles.value.map(_.getPath)

      val command =
        if (useCMake.value) {
          import java.io.File
          import java.io.PrintWriter

          val buildPath = nativeTarget.value / "_build"
          buildPath.mkdirs()

          val (flags, env) = toolchain match {
            case None =>
              val env =
                Seq(
                  ("CC", nativeCC.value),
                  ("CXX", nativeCXX.value),
                  ("CXXFLAGS", cxxflags.mkString(" "))
                )

              (Nil, env)

            case Some(toolchainPath) =>
              val toolchainFile = nativeTarget.value / "Toolchain.cmake"
              val out = new PrintWriter(toolchainFile)
              try {
                out.println(s"""
SET(CMAKE_SYSTEM_NAME Linux)
SET(CMAKE_C_COMPILER ${nativeCC.value})
SET(CMAKE_CXX_COMPILER ${nativeCXX.value})
SET(CMAKE_FIND_ROOT_PATH ${toolchainPath / "sysroot"})
SET(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
SET(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
add_definitions(-DANDROID)""")
              } finally {
                out.close()
              }

              val jniPath = toolchainPath / "sysroot" / "usr" / "include"
              if (!(jniPath / "jni.h").exists) {
                sys.error("JNI path does not contain jni.h: " + jniPath)
              }

              val flags =
                Seq(
                  "-DCMAKE_TOOLCHAIN_FILE=" + toolchainFile,
                  "-DJNI_H=" + jniPath,
                  "-DNEED_JNI_MD=n"
                )

              val pkgConfigPath = toolchainPath / "sysroot" / "usr" / "lib" / "pkgconfig"
              if (!pkgConfigPath.exists) {
                sys.error("pkg-config path does not exist: " + pkgConfigPath)
              }

              val env =
                Seq(
                  ("CXXFLAGS", cxxflags.mkString(" ")),
                  ("PATH",
                    System.getenv("PATH") +
                    File.pathSeparator +
                    (toolchainPath / "bin")),
                  ("PKG_CONFIG_PATH", pkgConfigPath.getPath)
                )

              (flags, env)
          }

          val cmake = {
            val dependenciesFile = {
              val fileName = nativeTarget.value / "Dependencies.cmake"
              val out = new PrintWriter(fileName)
              try {
                if (packageDependencies.value.nonEmpty) {
                  out.println("find_package(PkgConfig REQUIRED)")
                }
                packageDependencies.value foreach { pkg =>
                  val PKG = pkg.toUpperCase.replace('-', '_')
                  out.println(s"pkg_check_modules($PKG REQUIRED $pkg)")
                  out.println(s"include_directories($${${PKG}_INCLUDE_DIRS})")
                  out.println(s"link_directories($${${PKG}_LIBRARY_DIRS})")
                  out.println(s"link_libraries($${${PKG}_LIBRARIES})")
                }

                Seq(nativeSource.value, nativeTarget.value, managedNativeSource.value) foreach { dir =>
                  out.println(s"include_directories(${dir.getPath})")
                }
              } finally {
                out.close()
              }

              fileName
            }

            val targetFile = {
              val fileName = nativeTarget.value / "Target.cmake"
              val out = new PrintWriter(fileName)
              try {
                out.println(s"set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${binPath.value})")
                out.println(s"add_library(${libraryName.value} SHARED ${sources.mkString(" ")})")
              } finally {
                out.close()
              }

              fileName
            }

            Process(
              Seq(
                "cmake",
                "-DDEPENDENCIES_FILE=" + dependenciesFile.getPath,
                "-DTARGET_FILE=" + targetFile.getPath,
                baseDirectory.value.getPath
              ) ++ flags,
              buildPath,
              env:_*
            )
          }

          log.info(s"Configuring C++ build")
          checkExitCode(cmake, log)

          Process(Seq("make", "-j4"), buildPath)
        } else {
          val output = (binPath.value / System.mapLibraryName(libraryName.value)).getPath

          Process(Seq(nativeCXX.value, "-o", output) ++ cxxflags ++ ldflags ++ sources)
        }

      log.info(s"Compiling ${sources.size} C++ sources to ${binPath.value}")
      checkExitCode(command, log)
    }.dependsOn(javah)
     .tag(Tags.Compile, Tags.CPU)
     .value,


    compile <<= (compile in Compile, jniCompile).map((result, _) => result),

    cleanFiles ++= Seq(
      binPath.value,
      (headersPath in jniConfig).value
    ),

    // Make shared lib available at runtime. Must be used with forked JVM to work.
    javaOptions += s"-Djava.library.path=${binPath.value}",
    initialCommands in console := "im.tox.tox4j.JavaLibraryPath.addLibraryPath(\"" + binPath.value + "\")",
    // Required in order to have a separate JVM to set Java options.
    fork := true
  )
}

