import sbt.Keys._
import sbt._

import java.io.{File, PrintWriter}

import scala.language.postfixOps

object Jni extends Plugin {
  object Keys {

    // settings

    val libraryName = settingKey[String]("Shared library produced by JNI")

    val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")
    val versionSync = settingKey[String]("Package from pkg-config we want to sync our version number with")

    val nativeSource = settingKey[File]("JNI native sources")
    val nativeTarget = settingKey[File]("JNI native target directory")
    val managedNativeSource = settingKey[File]("Generated JNI native sources")

    val binPath = settingKey[File]("Shared libraries produced by JNI")

    val nativeCC = settingKey[String]("Compiler to use")
    val nativeCXX = settingKey[String]("Compiler to use")
    val toolchainPath = settingKey[Option[File]]("Optional toolchain location; must contain sysroot/ and bin/")
    val pkgConfigPath = settingKey[Seq[File]]("Directories to look in for pkg-config's .pc files")

    val ccOptions = settingKey[Seq[String]]("Flags to be passed to the native compiler when compiling")
    val ldOptions = settingKey[Seq[String]]("Flags to be passed to the native compiler when linking")
    val makeFlags = settingKey[Seq[String]]("Flags to be passed to make(1)")

    val jniClasses = settingKey[Seq[String]]("Classes with native methods")
    val jniSourceFiles = settingKey[Seq[File]]("JNI source files")

  }

  import Jni.Keys._


  private val jniConfig = config("native")

  private object PrivateKeys {

    // tasks

    val checkVersion = taskKey[Unit]("Check the versionSync variable")
    val javah = taskKey[Unit]("Generates JNI header files")
    val gtestPath = taskKey[File]("Finds the Google Test source path or downloads gtest from the internet")
    val cmakeDependenciesFile = taskKey[File]("Generates Dependencies.cmake containing C++ dependency information")
    val cmakeMainFile = taskKey[File]("Generates Main.cmake containing instructions for the main module")
    val cmakeTestFile = taskKey[File]("Generates Test.cmake containing instructions for the test module")
    val cmakeToolchainFlags = taskKey[Seq[String]]("Optionally generates Toolchain.cmake and returns the required cmake flags")
    val runCMake = taskKey[Unit]("Configures the build with CMake")
    val jniCompile = taskKey[Unit]("Compiles JNI native sources")

    // settings

    val headersPath = settingKey[File]("Generated JNI headers")
    val includes = settingKey[Seq[File]]("Compiler include directories")

  }

  import Jni.PrivateKeys._


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

  private def pkgConfig(query: String, pkgs: Seq[String], paths: Seq[File]) = {
    def pkg_config(args: Seq[String]) = {
      val origPath =
        Option(System.getenv("PKG_CONFIG_PATH"))
          .map(_.split(File.pathSeparator).toSeq)
          .getOrElse(Nil)

      Process(
        Seq("pkg-config") ++ args,
        None,
        (
          ("PKG_CONFIG_PATH", (paths ++ origPath).mkString(File.pathSeparator))
        )
      )
    }

    pkgs map { pkg =>
      (pkg, pkg_config(Seq(pkg)) !< nullLog != 0)
    } filter (_._2) map (_._1) match {
      case Nil =>
      case missing =>
        sys.error(s"missing ${missing.size} packages: ${missing.mkString(", ")}")
    }

    pkgs match {
      case Nil =>
        Nil
      case _ =>
        val command = pkg_config(Seq("--" + query) ++ pkgs)
        (command !!).split(" ").map(_.trim).filter(!_.isEmpty).toSeq
    }
  }

  private def pkgConfig(pkg: String, paths: Seq[File]): String = {
    pkgConfig("modversion", Seq(pkg), paths).head
  }


  private def mkToolchain(toolchainPath: Option[File], tools: Seq[String]) = {
    val prefixTools =
      toolchainPath map { toolchainPath =>
        val triple = toolchainPath.getName
        tools map { tool => (toolchainPath / "bin" / s"$triple-$tool").getPath }
      } getOrElse Nil
    prefixTools ++ tools
  }

  private def findTool(toolchainPath: Option[File], candidates: String*) = {
    mkToolchain(toolchainPath, candidates) find { cc =>
      try {
        Seq(cc, "--version") !< nullLog == 0
      } catch {
        case _: java.io.IOException => false
      }
    } getOrElse "false"
  }

  private def findCc(toolchainPath: Option[File]) = findTool(toolchainPath, "clang", "gcc", "cc")
  private def findCxx(toolchainPath: Option[File]) = findTool(toolchainPath, "clang++", "g++", "c++")

  private def checkCcOptions(compiler: String, code: String, flags: Seq[String]*) = {
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


  private def mkPkgConfigPath(pkgConfigPath: Seq[File], toolchainPath: Option[File]) = {
    pkgConfigPath ++ {
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

    includes ++= jreInclude(toolchainPath.value).getOrElse(Nil),

    // Check modversion
    checkVersion := Def.task {
      val log = streams.value.log

      versionSync.value match {
        case "" =>
        case pkg =>
          val pkgVersion = pkgConfig(pkg, mkPkgConfigPath(pkgConfigPath.value, toolchainPath.value))
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
    pkgConfigPath := Nil,
    versionSync := "",

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

    // Empty sequences by default.
    ccOptions := Nil,
    ldOptions := Nil,

    // Build with parallel tasks by default.
    makeFlags := Seq("-j" + java.lang.Runtime.getRuntime.availableProcessors),

    // Shared library flags.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fPIC")),

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

    // Ignore C++14 extension warnings.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-Wno-c++1y-extensions")),

    // No RTTI and no exceptions.
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fno-exceptions")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-fno-rtti")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-DGOOGLE_PROTOBUF_NO_RTTI")),
    ccOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-DGTEST_HAS_RTTI=0")),

    // Error on undefined references in shared object.
    ldOptions ++= checkCcOptions(nativeCXX.value, "", Seq("-Wl,-z,defs")),

    jniSourceFiles in Compile := filterNativeSources(((nativeSource in Compile).value ** "*").get),
    jniSourceFiles in Test := filterNativeSources(((nativeSource in Test).value ** "*").get),


    javah := Def.task {
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


    cmakeDependenciesFile := Def.task {
      val fileName = nativeTarget.value / "Dependencies.cmake"
      val out = new PrintWriter(fileName)
      try {
        for (dir <- (includes in jniConfig).value)
          out.println(s"include_directories($dir)")

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

        Seq((nativeSource in Compile).value, nativeTarget.value, managedNativeSource.value) foreach { dir =>
          out.println(s"include_directories($dir)")
        }
      } finally {
        out.close()
      }

      fileName
    }.value,


    cmakeMainFile := Def.task {
      val fileName = nativeTarget.value / "Main.cmake"
      val out = new PrintWriter(fileName)
      try {
        val mainSources = (jniSourceFiles in Compile).value

        out.println(s"set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${binPath.value})")
        out.println(s"add_library(${libraryName.value} SHARED ${mainSources.mkString(" ")})")
      } finally {
        out.close()
      }

      fileName
    }.value,


    gtestPath := Def.task {
      val log = streams.value.log

      val candidates = Seq(
        file("/usr/src/gtest")
      )

      candidates find { candidate =>
        (candidate / "src" / "gtest-all.cc").exists
      } getOrElse {
        val gtestDir = managedNativeSource.value / "gtest"
        if (!gtestDir.exists) {
          val command = Seq(
            "svn", "checkout",
            "http://googletest.googlecode.com/svn/trunk/",
            gtestDir.getPath
          )

          log.info("Fetching gtest sources")
          checkExitCode(command, log)
        }

        gtestDir
      }
    }.value,


    cmakeTestFile := Def.task {
      val fileName = nativeTarget.value / "Test.cmake"
      val out = new PrintWriter(fileName)
      try {
        val gtestDir = gtestPath.value

        out.println(s"add_library(gtest STATIC $gtestDir/src/gtest-all.cc)")
        out.println(s"include_directories($gtestDir $gtestDir/include)")

        out.println("link_libraries(gtest)")

        val testSources = (jniSourceFiles in Test).value

        out.println(s"add_executable(${libraryName.value}_test ${testSources.mkString(" ")})")
        out.println(s"#add_test(${libraryName.value}_test ${libraryName.value}_test)")

        testSources.foreach { source =>
          if (source.getName != "main.cpp") {
            import org.apache.commons.io.FilenameUtils

            val testName = FilenameUtils.removeExtension(source.getName)
            out.println(s"add_executable($testName main.cpp $source)")
            out.println(s"add_test($testName $testName)")
          }
        }
      } finally {
        out.close()
      }

      fileName
    }.value,


    cmakeToolchainFlags := Def.task {
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
    }.value,


    runCMake := Def.task {
      val log = streams.value.log

      // Make sure the output directory exists.
      binPath.value.mkdirs()

      val cxxflags = ccOptions.value.distinct
      val ldflags = ldOptions.value.distinct

      val pkgConfigDirs =
        mkPkgConfigPath(pkgConfigPath.value, toolchainPath.value).mkString(File.pathSeparator)

      val buildPath = nativeTarget.value / "_build"
      buildPath.mkdirs()

      val env = toolchainPath.value match {
        case None =>
          Seq(
            ("CC", nativeCC.value),
            ("CXX", nativeCXX.value),
            ("CXXFLAGS", cxxflags.mkString(" ")),
            ("LDFLAGS", ldflags.mkString(" ")),
            ("PKG_CONFIG_PATH", pkgConfigDirs)
          )

        case Some(toolchainPath) =>
          Seq(
            ("CXXFLAGS", cxxflags.mkString(" ")),
            ("LDFLAGS", ldflags.mkString(" ")),
            ("PATH",
              System.getenv("PATH") +
              File.pathSeparator +
              (toolchainPath / "bin")),
            ("PKG_CONFIG_PATH", pkgConfigDirs)
          )
      }

      val flags = cmakeToolchainFlags.value

      val cmake = {
        Process(
          Seq(
            "cmake",
            "-DDEPENDENCIES_FILE=" + cmakeDependenciesFile.value,
            "-DMAIN_FILE=" + cmakeMainFile.value,
            "-DTEST_FILE=" + cmakeTestFile.value,
            baseDirectory.value.getPath
          ) ++ flags,
          buildPath,
          env:_*
        )
      }

      log.info(s"Configuring C++ build")
      checkExitCode(cmake, log)
    }.dependsOn(javah)
     .tag(Tags.Compile, Tags.CPU)
     .value,


    jniCompile := Def.task {
      val log = streams.value.log

      val buildPath = nativeTarget.value / "_build"
      val mainSources = (jniSourceFiles in Compile).value

      val command = Process(Seq("make") ++ makeFlags.value, buildPath)

      log.info(s"Compiling ${mainSources.size} C++ sources to ${binPath.value}")
      checkExitCode(command, log)
    }.dependsOn(javah, runCMake)
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

