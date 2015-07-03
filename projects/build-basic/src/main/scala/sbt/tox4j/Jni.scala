package sbt.tox4j

import java.io.{ File, PrintWriter }

import org.apache.commons.io.FilenameUtils
import sbt.Keys._
import sbt._
import sbt.tox4j.logic.CMakeGenerator
import sbt.tox4j.util.NativeFinder

import scala.language.postfixOps

// scalastyle:off
object Jni extends OptionalPlugin {

  private val DEBUG = false
  private val coverageEnabled = true

  val Native = config("native")

  object BuildTool {
    sealed trait T { def name: String; def command: String }
    case object Ninja extends T { def name = "Ninja"; def command = "ninja" }
    case object Make extends T { def name = "Unix Makefiles"; def command = "make" }
  }

  object Keys {

    // settings

    val libraryName = settingKey[String]("Shared library produced by JNI")

    val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")

    val nativeSource = settingKey[File]("JNI native sources")
    val nativeTarget = settingKey[File]("JNI native target directory")
    val managedNativeSource = settingKey[File]("Generated JNI native sources")

    val binPath = settingKey[File]("Shared libraries produced by JNI")

    val nativeCC = settingKey[String]("Compiler to use")
    val nativeCXX = settingKey[String]("Compiler to use")
    val toolchainPath = settingKey[Option[File]]("Optional toolchain location; must contain sysroot/ and bin/")
    val pkgConfigPath = settingKey[Seq[File]]("Directories to look in for pkg-config's .pc files")

    val cppFlags = settingKey[Seq[String]]("Flags to be passed to the preprocessor when compiling native code")
    val cFlags = settingKey[Seq[String]]("Flags to be passed to the native C compiler when compiling")
    val cxxFlags = settingKey[Seq[String]]("Flags to be passed to the native C++ compiler when compiling")
    val ldFlags = settingKey[Seq[String]]("Flags to be passed to the native compiler when linking")

    val coverageFlags = settingKey[Seq[String]]("Flags to be passed to the native compiler to enable coverage recording")

    val buildTool = settingKey[BuildTool.T]("Build tool to use [make, ninja]")
    val buildFlags = settingKey[Seq[String]]("Flags to be passed to the build tool")

    val jniClasses = taskKey[Map[String, Seq[String]]]("Classes with native methods")
    val jniSourceFiles = settingKey[Seq[File]]("JNI source files")
    val jniCompile = taskKey[Seq[File]]("Compiles JNI native sources")

  }

  import Keys._

  private object PrivateKeys {

    // tasks

    val javah = taskKey[Seq[String]]("Generates JNI header files")
    val gtestPath = taskKey[Option[File]]("Finds the Google Test source path or downloads gtest from the internet")
    val cmakeDependenciesFile = taskKey[File]("Generates Dependencies.cmake containing C++ dependency information")
    val cmakeMainFile = taskKey[File]("Generates Main.cmake containing instructions for the main module")
    val cmakeTestFile = taskKey[Option[File]]("Generates Test.cmake containing instructions for the test module")
    val cmakeToolchainFlags = taskKey[Seq[String]]("Optionally generates Toolchain.cmake and returns the required cmake flags")
    val runCMake = taskKey[File]("Configures the build with CMake")

    // settings

    val headersPath = settingKey[File]("Generated JNI headers")
    val includes = settingKey[Seq[File]]("Compiler include directories")

  }

  import PrivateKeys._

  private object nullLog extends AnyRef with ProcessLogger {
    def buffer[T](f: => T): T = f
    def error(s: => String) = {}
    def info(s: => String) = {}
  }

  private object stdoutLog extends AnyRef with ProcessLogger {
    def buffer[T](f: => T): T = f
    def error(s: => String) = { println(s) }
    def info(s: => String) = { println(s) }
  }

  val debugLog =
    if (DEBUG) {
      stdoutLog
    } else {
      nullLog
    }

  /**
   * Extensions of source files.
   */
  private val cppExtensions = Seq(".cpp", ".cc", ".cxx", ".c")

  private def isNativeSource(file: File): Boolean = {
    file.isFile && cppExtensions.exists(file.getName.toLowerCase.endsWith)
  }

  private val jdkHome = {
    val home = file(sys.props("java.home"))
    if (home.exists)
      Some(home.getAbsoluteFile)
    else
      None
  }

  private def jreInclude(toolchainPath: Option[File]) = {
    toolchainPath match {
      case Some(_) => None
      case None =>
        jdkHome.map { home =>
          val absHome = home.getParentFile
          // In a typical installation, JDK files are one directory above the
          // location of the JRE set in 'java.home'.
          Seq(absHome / "include")
        }
    }
  }

  private def findCcOptions(compiler: String, required: Boolean = false, code: String = "")(flags: Seq[String]*) = {
    val sourceFile = File.createTempFile("configtest", cppExtensions.head)
    val targetFile = File.createTempFile("configtest", ".out")

    try {
      val out = new PrintWriter(sourceFile)
      try {
        out.println(code)
        out.println("int main () { return 0; }")
      } finally {
        out.close()
      }

      val found =
        flags find { flags =>
          Seq(compiler, sourceFile.getPath, "-o", targetFile.getPath, "-Werror") ++ flags !< debugLog match {
            case 0 => true
            case _ => false
          }
        }

      if (found.isEmpty && required) {
        sys.error(s"No valid flags found with compiler $compiler; tried [${flags.map(_.mkString).mkString("; ")}]")
      }

      found
    } finally {
      targetFile.delete()
      sourceFile.delete()
      file(FilenameUtils.removeExtension(sourceFile.getAbsolutePath) + ".gcno").delete()
    }
  }

  private def checkCcOptions(compiler: String, required: Boolean = false, code: String = "")(flags: Seq[String]*) =
    findCcOptions(compiler, required, code)(flags: _*) match {
      case None        => Nil
      case Some(flags) => flags
    }

  private def mkToolchain(toolchainPath: Option[File], tools: Seq[String]) = {
    toolchainPath match {
      case Some(toolchainPath) =>
        val triple = toolchainPath.getName
        (tools map { tool => (toolchainPath / "bin" / s"$triple-$tool").getPath }) ++ tools
      case None =>
        tools
    }
  }

  private def findTool(toolchainPath: Option[File], code: String)(flags: Seq[String]*)(candidates: Seq[String]) = {
    mkToolchain(toolchainPath, candidates) find { cc =>
      try {
        val flagsOpt = Seq[String]() +: flags
        debugLog.info(s"Trying $cc")
        Seq(cc, "--version") !< debugLog == 0 && findCcOptions(cc, false, code)(flagsOpt: _*) != None
      } catch {
        case _: java.io.IOException => false
      }
    } getOrElse "false"
  }

  private def findCc(toolchainPath: Option[File]) = findTool(
    toolchainPath,
    // Check for a working C89 compiler.
    """
    int foo(void);
    """
  )(
      Seq("-std=c89")
    )(sys.env.get("CC").toSeq ++ Seq("clang-3.5", "clang35", "gcc-4.9", "clang", "gcc", "cc"))

  private def findCxx(toolchainPath: Option[File]) = findTool(
    toolchainPath,
    // Check for a working C++14 compiler.
    """
    auto f = [](auto i) mutable { return i; };

    template<typename... Args>
    int bar (Args ...args) { return sizeof... (Args); }

    template<typename... Args>
    auto foo (Args ...args) {
      return [&] { return bar (args...); };
    }
    """
  )(
      Seq("-std=c++14"),
      Seq("-std=c++1y")
    )(sys.env.get("CXX").toSeq ++ Seq("clang++-3.5", "clang35++", "g++-4.9", "clang++", "g++", "c++"))

  private def checkExitCode(command: ProcessBuilder, log: Logger) = {
    command ! log match {
      case 0 =>
      case exitCode =>
        sys.error(s"command failed with exit code $exitCode:\n  $command")
    }
  }

  private def mkPkgConfigPath(pkgConfigPath: Seq[File], toolchainPath: Option[File]) = {
    {
      toolchainPath map (_ / "sysroot" / "usr" / "lib" / "pkgconfig") match {
        case Some(toolchainPath) =>
          if (toolchainPath.exists) {
            Seq(toolchainPath)
          } else {
            Nil
          }
        case None =>
          Nil
      }
    } ++ pkgConfigPath
  }

  object Platform {
    sealed trait T
    case class Android(platform: String) extends T
    case object Host extends T

    // Android build:
    private def androidSettings(platform: String) = Seq(
      toolchainPath := {
        val candidates = Seq(
          baseDirectory.value / "android" / platform,
          baseDirectory.value.getParentFile / "android" / platform
        )
        // Try $TOOLCHAIN first, then try some other possible candidate paths.
        sys.env.get("TOOLCHAIN").map(file).orElse(candidates.find(_.exists))
      },
      pkgConfigPath := toolchainPath.value.map(_ / "sysroot/usr/lib/pkgconfig").toSeq,
      cppFlags := Nil,
      ldFlags := Nil,

      jniSourceFiles in Compile += {
        def ifExists(file: File): Option[File] = {
          Option(file).filter(_.exists)
        }

        val home = file(sys.env("HOME"))

        val candidates = Seq(
          sys.env.get("ANDROID_NDK_HOME").map(file).flatMap(ifExists),
          ifExists(home / "usr/android-ndk"),
          ifExists(home / "android-ndk")
        )

        candidates.find(_.nonEmpty).flatten match {
          case Some(ndkHome) =>
            val cpufeatures = ndkHome / "sources/android/cpufeatures/cpu-features.c"
            if (!cpufeatures.exists) {
              sys.error("Could not find cpu-features.c required for the Android build")
            }
            cpufeatures
          case None =>
            sys.error("Could not find Android NDK (you may need to set the ANDROID_NDK_HOME env var)")
        }
      }
    )

    private def jniSettings(target: T) =
      target match {
        case Android(platform) => androidSettings(platform)
        case Host              => Nil
      }

    private val platform =
      sys.env.get("TOX4J_TARGET") match {
        case Some("host") | None => Host
        case Some(target) =>
          if (target.contains("android"))
            Android(target)
          else
            sys.error("Unknown target: " + target)
      }

    val settings = jniSettings(platform)
  }

  override val moduleSettings = Seq(
    inConfig(Native)(Seq[Setting[_]](

      jniClasses := {
        val classes = (compileIncremental in Compile).value
          .analysis
          .relations
          .allProducts
          .filter(_.name.endsWith(".class"))
          .toSet
        NativeFinder.natives(classes)
      },

      // Target for javah-generated headers.
      headersPath := nativeTarget.value / "include",

      // Include directories.
      includes := Nil,

      includes ++= Seq(
        headersPath.value,
        (nativeSource in Compile).value,
        (managedNativeSource in Compile).value
      ),

      includes ++= jreInclude(toolchainPath.value).getOrElse(Nil)
    )),

    Seq[Setting[_]](
      // Library name defaults to the project name.
      libraryName := name.value,

      // Initialise pkg-config dependencies to the empty sequence.
      packageDependencies := Nil,
      pkgConfigPath := sys.env.get("PKG_CONFIG_PATH").map(_.split(File.pathSeparator).toSeq.map(file)).getOrElse(Nil),

      // Native source directory defaults to "src/main/cpp".
      nativeSource in Compile := (sourceDirectory in Compile).value / "cpp",
      nativeSource in Test := (sourceDirectory in Test).value / "cpp",
      nativeTarget := (target in Compile).value / "cpp",
      managedNativeSource := nativeTarget.value / "source",

      // Put the linked library in here.
      binPath := nativeTarget.value / "bin",

      // Default to global toolchain.
      toolchainPath := None,

      // Default native C++ compiler to Clang.
      nativeCC := findCc(toolchainPath.value),
      nativeCXX := findCxx(toolchainPath.value),

      // Defaults from the environment.
      cppFlags := sys.env.get("CPPFLAGS").toSeq,
      cFlags := sys.env.get("CFLAGS").toSeq,
      cxxFlags := sys.env.get("CXXFLAGS").toSeq,
      ldFlags := sys.env.get("LDFLAGS").toSeq,

      // Build with parallel tasks by default.
      buildTool := {
        import BuildTool._

        Seq(Ninja, Make).foldLeft[Option[T]](None) { (found, next) =>
          found match {
            case Some(_) => found
            case None =>
              try {
                Seq(next.command, "--version") !< nullLog
                Some(next)
              } catch {
                case _: java.io.IOException => None
              }
          }
        } getOrElse Make
      },
      buildFlags := Seq("-j" + java.lang.Runtime.getRuntime.availableProcessors),

      // C++14 flags.
      cxxFlags ++= checkCcOptions(nativeCXX.value)(
        Seq("-std=c++14"),
        Seq("-std=c++1y")
      ),

      // Debug flags.
      cxxFlags ++= checkCcOptions(nativeCXX.value)(
        Seq("-ggdb3"),
        Seq("-g3"),
        Seq("-g")
      ),

      // Warning flags.
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-Wall")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-Wextra")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-pedantic")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-fcolor-diagnostics")),

      // Use libc++ if available.
      //ccOptions ++= checkCcOptions(nativeCXX.value)(Seq("-stdlib=libc++")),

      // No RTTI and no exceptions.
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-fno-exceptions")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-fno-rtti")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-DGOOGLE_PROTOBUF_NO_RTTI")),
      cxxFlags ++= checkCcOptions(nativeCXX.value)(Seq("-DGTEST_HAS_RTTI=0")),

      // Error on undefined references in shared object.
      ldFlags ++= checkCcOptions(nativeCXX.value)(Seq("-Wl,-z,defs")),

      // Enable test coverage collection.
      coverageFlags := checkCcOptions(nativeCXX.value)(Seq("-fprofile-arcs", "-ftest-coverage")),

      jniSourceFiles in Compile := ((nativeSource in Compile).value ** "*").filter(isNativeSource).get,
      jniSourceFiles in Test := ((nativeSource in Test).value ** "*").filter(isNativeSource).get,

      cleanFiles ++= Seq(
        binPath.value,
        (headersPath in Native).value
      ),

      // Make shared lib available at runtime. Must be used with forked JVM to work.
      javaOptions ++= Seq(
        s"-Djava.library.path=${binPath.value}",
        "-Xmx1g"
      ),
      initialCommands in console := "im.tox.tox4j.JavaLibraryPath.addLibraryPath(\"" + binPath.value + "\")",
      // Required in order to have a separate JVM to set Java options.
      fork := true
    ),

    Platform.settings,

    inConfig(Native)(Seq[Setting[_]](
      javah := Def.task {
        val log = streams.value.log

        val result = (compileIncremental in Compile).value

        val classpath = (
          (dependencyClasspath in Compile).value.files ++
          Seq((classDirectory in Compile).value)
        ).mkString(File.pathSeparator)

        val jniClassNames = jniClasses.value.keys.toSeq

        val command = Seq(
          "javah",
          "-d", headersPath.value.getPath,
          "-classpath", classpath
        ) ++ jniClassNames

        log.info(s"Running javah to generate ${jniClassNames.size} JNI headers")
        checkExitCode(command, log)

        jniClassNames
      }.tag(Tags.Compile, Tags.CPU)
        .value,

      cmakeDependenciesFile := {
        val action = CMakeGenerator.dependenciesFile(
          includes.value,
          packageDependencies.value,
          (nativeSource in Compile).value,
          nativeTarget.value,
          managedNativeSource.value
        )

        action()
      },

      cmakeMainFile := {
        val action = CMakeGenerator.mainFile(
          binPath.value,
          libraryName.value,
          nativeTarget.value,
          (jniSourceFiles in Compile).value
        )

        action()
      },

      gtestPath := {
        val log = streams.value.log

        val candidates = Seq(
          file("/usr/src/gtest")
        )

        candidates find { candidate =>
          (candidate / "src" / "gtest-all.cc").exists
        } match {
          case Some(gtestDir) => Some(gtestDir)
          case None =>
            val gtestDir = managedNativeSource.value / "gtest"
            if (!gtestDir.exists) {
              val command = Seq(
                "svn", "checkout",
                "http://googletest.googlecode.com/svn/trunk/",
                gtestDir.getPath
              )

              log.info("Fetching gtest sources")
              command ! log match {
                case 0 =>
                  Some(gtestDir)
                case exitCode =>
                  log.info(s"command failed with exit code $exitCode:\n  $command")
                  None
              }
            } else {
              Some(gtestDir)
            }
        }
      },

      cmakeTestFile := {
        gtestPath.value.map { gtestDir =>
          val fileName = nativeTarget.value / "Test.cmake"
          val out = new PrintWriter(fileName)
          try {
            out.println(s"add_library(gtest STATIC $gtestDir/src/gtest-all.cc)")
            out.println(s"include_directories($gtestDir $gtestDir/include)")

            out.println("link_libraries(gtest)")

            val testSources = (jniSourceFiles in Test).value

            out.println(s"add_executable(${libraryName.value}_test ${testSources.mkString(" ")})")
            out.println(s"#add_test(${libraryName.value}_test ${libraryName.value}_test)")

            testSources.foreach { source =>
              if (source.getName != "main.cpp" && source.getName != "mock_jni.cpp") {
                val testName = FilenameUtils.removeExtension(source.getName)
                out.println(s"add_executable($testName main.cpp mock_jni.cpp $source)")
                out.println(s"add_test($testName $testName)")
              }
            }
          } finally {
            out.close()
          }

          fileName
        }
      },

      cmakeToolchainFlags := {
        toolchainPath.value match {
          case None =>
            Nil

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
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)""")
            } finally {
              out.close()
            }

            val jniPath = toolchainPath / "sysroot" / "usr" / "include"
            if (!(jniPath / "jni.h").exists) {
              sys.error("JNI path does not contain jni.h: " + jniPath)
            }

            val needJniMd =
              if ((jniPath / "jni_md.h").exists)
                "y"
              else
                "n"

            Seq(
              "-DCMAKE_TOOLCHAIN_FILE=" + toolchainFile,
              "-DJNI_H=" + jniPath,
              "-DNEED_JNI_MD=" + needJniMd
            )
        }
      },

      runCMake := Def.task {
        val log = streams.value.log

        // Make sure the output directory exists.
        binPath.value.mkdirs()

        val coverageflags =
          if (coverageEnabled) {
            log.info(s"Coverage enabled: adding ${coverageFlags.value} to CXXFLAGS and LDFLAGS")
            coverageFlags.value
          } else {
            Nil
          }

        val cppflags = cppFlags.value.distinct
        val cflags = cFlags.value.distinct
        val cxxflags = cxxFlags.value.distinct ++ coverageflags
        val ldflags = ldFlags.value.distinct ++ coverageflags

        val pkgConfigDirs =
          mkPkgConfigPath(pkgConfigPath.value, toolchainPath.value).mkString(File.pathSeparator)

        val buildPath = nativeTarget.value / "_build"
        buildPath.mkdirs()

        val env = (toolchainPath.value match {
          case None =>
            Seq(
              ("CC", nativeCC.value),
              ("CXX", nativeCXX.value)
            )

          case Some(toolchainPath) =>
            Seq(
              ("PATH",
                sys.env("PATH") +
                File.pathSeparator +
                (toolchainPath / "bin"))
            )
        }) ++ Seq(
          ("CPPFLAGS", cppflags.mkString(" ")),
          ("CFLAGS", cflags.mkString(" ")),
          ("CXXFLAGS", cxxflags.mkString(" ")),
          ("LDFLAGS", ldflags.mkString(" ")),
          ("PKG_CONFIG_PATH", pkgConfigDirs)
        )

        val flags = cmakeToolchainFlags.value

        val cmake = {
          Process(
            Seq(
              "cmake", "-G" + buildTool.value.name,
              "-DDEPENDENCIES_FILE=" + cmakeDependenciesFile.value,
              "-DMAIN_FILE=" + cmakeMainFile.value,
              baseDirectory.value.getPath
            ) ++ flags ++ cmakeTestFile.value.map("-DTEST_FILE=" + _).toSeq,
            buildPath,
            env: _*
          )
        }

        log.info("Configuring C++ build")
        checkExitCode(cmake, log)

        buildPath
      }.dependsOn(javah)
        .tag(Tags.Compile, Tags.CPU)
        .value,

      jniCompile := Def.task {
        val log = streams.value.log

        val buildPath = runCMake.value
        val mainSources = (jniSourceFiles in Compile).value

        val command = Process(buildTool.value.command +: buildFlags.value, buildPath)

        log.info(s"Compiling ${mainSources.size} C++ sources to ${binPath.value}")
        checkExitCode(command, log)

        Seq("dll", "dylib", "so").map("*." + _).map(binPath.value ** _).flatMap(_.get)
      }.dependsOn(javah)
        .tag(Tags.Compile, Tags.CPU)
        .value
    )),

    Seq[Setting[_]](
      (compile in Compile) <<= (compile in Compile).dependsOn(jniCompile in Native),
      (test in Test) <<= (test in Test).dependsOn(jniCompile in Native)
    )
  ).flatten

}
