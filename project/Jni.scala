import sbt._
import Keys._

import scala.language.postfixOps

object Jni extends Plugin {
  object Keys {

    // tasks

    val jniCompile = taskKey[Unit]("Compiles JNI native sources")
    val javah = taskKey[Unit]("Generates JNI header files")

    // settings

    val useCMake = settingKey[Boolean]("Use CMake instead of built-in C++ build system")

    val libraryName = settingKey[String]("Shared library produced by JNI")

    val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")

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
      Some(home)
    else
      None
  }

  private val jreInclude = {
    jdkHome.map { home =>
      val absHome = home.getAbsoluteFile.getParentFile
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
      case pkgs =>
        val command = Seq("pkg-config", "--" + query) ++ pkgs
        (command !!).split(" ").map(_.trim).filter(!_.isEmpty).toSeq
    }
  }


  private val toolchain = Option(System.getenv("TOOLCHAIN")) map file
  private def mkToolchain(tools: Seq[String]) = {
    val prefixTools =
      toolchain map { toolchain =>
        val triple = toolchain.getName
        (tools map { tool => (toolchain / "bin" / s"$triple-$tool").getAbsolutePath })
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

    headersPath := nativeTarget.value / "include",

    includes := Nil,

    includes ++= Seq(
      headersPath.value,
      (nativeSource in Compile).value,
      (managedNativeSource in Compile).value
    ),
  
    includes ++= jreInclude.getOrElse(Nil)

  )) ++ Seq(

    // Library name defaults to the project name.
    libraryName := name.value,

    // Initialise pkg-config dependencies to the empty sequence.
    packageDependencies := Nil,

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

          val crossCompilation = toolchain.map { toolchain =>
            val toolchainFile = nativeTarget.value / "Toolchain.cmake"
            val out = new PrintWriter(toolchainFile)
            try {
              out.print(s"""
SET(CMAKE_SYSTEM_NAME Linux)
SET(CMAKE_C_COMPILER ${nativeCC.value})
SET(CMAKE_CXX_COMPILER ${nativeCXX.value})
SET(CMAKE_FIND_ROOT_PATH ${toolchain / "sysroot"})
SET(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
SET(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
add_definitions(-DANDROID)
                """)
            } finally {
              out.close()
            }

            val jniPath = toolchain / "sysroot" / "usr" / "include"
            if (!(jniPath / "jni.h").exists) {
              sys.error("JNI path does not contain jni.h: " + jniPath)
            }

            Seq(
              "-DCMAKE_TOOLCHAIN_FILE=" + toolchainFile.getAbsolutePath,
              "-DJNI_H=" + jniPath.getAbsolutePath,
              "-DNEED_JNI_MD=n"
            )
          }

          val env =
            toolchain match {
              case None =>
                Seq(
                  ("CC", nativeCC.value),
                  ("CXX", nativeCXX.value),
                  ("CXXFLAGS", cxxflags.mkString(" "))
                )
              case Some(toolchain) =>
                val pkgConfigPath = toolchain / "sysroot" / "usr" / "lib" / "pkgconfig"
                if (!pkgConfigPath.exists) {
                  sys.error("pkg-config path does not exist: " + pkgConfigPath)
                }
                Seq(
                  ("CXXFLAGS", cxxflags.mkString(" ")),
                  ("PATH",
                    System.getenv("PATH") +
                    File.pathSeparator +
                    (toolchain / "bin").getAbsolutePath),
                  ("PKG_CONFIG_PATH",
                    pkgConfigPath.getAbsolutePath)
                )
            }

          val cmake = Process(
            Seq(
              "cmake",
              "-DLIB_TARGET_NAME=" + libraryName.value,
              "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=" + binPath.value,
              baseDirectory.value.getPath
            ) ++ crossCompilation.getOrElse(Nil),
            buildPath,
            env:_*
          )

          log.info(s"Configuring C++ build")
          checkExitCode(cmake, log)

          Process(Seq("make"), buildPath)
        } else {
          val output = (binPath.value / System.mapLibraryName(libraryName.value)).getPath

          Process(Seq(nativeCXX.value, "-o", output) ++ cxxflags ++ ldflags ++ sources)
        }

      log.info(s"Compiling ${sources.size} C++ sources to ${binPath.value}")
      checkExitCode(command, log)
    }.dependsOn(javah)
     .tag(Tags.Compile, Tags.CPU)
     .value,

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
     .tag(Tags.Compile, Tags.CPU)
     .value,

    compile <<= (compile in Compile, jniCompile).map((result, _) => result),

    cleanFiles ++= Seq( 
      binPath.value,
      (headersPath in jniConfig).value
    ),

    // Make shared lib available at runtime. Must be used with forked JVM to work.
    javaOptions += s"-Djava.library.path=${binPath.value}",
    // Required in order to have a separate JVM to set Java options.
    fork := true
  )
}

