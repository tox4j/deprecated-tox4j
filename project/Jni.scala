import sbt._
import Keys._

import scala.language.postfixOps

object Jni extends Plugin {
  object Keys {

    // tasks

    val jniCompile = taskKey[Unit]("Compiles JNI native sources")
    val javah = taskKey[Unit]("Generates JNI header files")

    // settings

    val libraryName = settingKey[String]("Shared library produced by JNI")

    val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")

    val nativeSource = settingKey[File]("JNI native sources")
    val nativeTarget = settingKey[File]("JNI native target directory")
    val managedNativeSource = settingKey[File]("Generated JNI native sources")

    val binPath = settingKey[File]("Shared libraries produced by JNI")

    val nativeCompiler = settingKey[String]("Compiler to use")

    val ccOptions = settingKey[Seq[String]]("Flags to be passed to the native compiler")

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

  private val jreIncludeFolder = {
    import grizzled.sys._
    os match {
      case OperatingSystem.Posix => "linux"
      case OperatingSystem.Mac => "darwin"
    }
  }

  private val jreIncludes = {
    jdkHome.map { home =>
      val absHome = home.getAbsoluteFile.getParentFile
      // In a typical installation, JDK files are one directory above the
      // location of the JRE set in 'java.home'.
      Seq(
        absHome / "include",
        absHome / "include" / jreIncludeFolder
      )
    }
  }

  private def executableName(name: String) = {
    import grizzled.sys._
    os match {
      case OperatingSystem.Posix => name
      case OperatingSystem.Mac => name
      case OperatingSystem.Windows => name + ".exe"
    }
  }

  private def sharedLibraryName(name: String) = {
    import grizzled.sys._
    os match {
      case OperatingSystem.Posix => "lib" + name + ".so"
      case OperatingSystem.Mac => "lib" + name + ".dylib"
      case OperatingSystem.Windows => name + ".dll"
    }
  }

  private def pkgConfig(pkgs: Seq[String]) = {
    pkgs match {
      case Nil =>
        Nil
      case pkgs =>
        val command = Seq("pkg-config", "--cflags", "--libs") ++ pkgs
        (command !!).split(" ").map(_.trim).filter(!_.isEmpty).toSeq
    }
  }

  private def findCc() = {
    Seq("clang++", "g++", "c++") find { cc =>
      try {
        Seq(cc, "--version") !< nullLog == 0
      } catch {
        case _: java.io.IOException => false
      }
    } getOrElse "false"
  }

  private def checkCcOptions(compiler: String, code: String, flags: Seq[String]*) = {
    import java.io.File
    import java.io.PrintWriter

    val sourceFile = File.createTempFile("configtest", cppExtensions(0))
    val out = new PrintWriter(sourceFile)
    out.println(code)
    out.println("int main () { return 0; }")
    out.close()

    val targetFile = File.createTempFile("configtest", ".out")

    try {
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

  private def checkExitCode(command: Seq[String], log: Logger) = {
    command ! log match {
      case 0 =>
      case exitCode =>
        sys.error(s"command failed with exit code ${exitCode}:\n  ${command.mkString(" ")}")
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
  
    includes ++= jreIncludes.getOrElse(Nil)

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
    nativeCompiler := findCc(),

    // Check for some C++ compiler flags.
    ccOptions := Nil,
    ccOptions ++= checkCcOptions(nativeCompiler.value, "",
      Seq("-fPIC", "-shared")
    ),
    ccOptions ++= checkCcOptions(nativeCompiler.value, "auto f = []{};",
      Seq("-std=c++11"),
      Seq("-std=c++0x")
    ),

    // Warning flags.
    ccOptions ++= checkCcOptions(nativeCompiler.value, "", Seq("-Wall")),
    ccOptions ++= checkCcOptions(nativeCompiler.value, "", Seq("-Wextra")),
    ccOptions ++= checkCcOptions(nativeCompiler.value, "", Seq("-pedantic")),

    // Include directories.
    ccOptions ++= (includes in jniConfig).value.map("-I" + _),
    // pkg-config flags.
    ccOptions ++= pkgConfig(packageDependencies.value),

    jniSourceFiles := filterNativeSources((nativeSource.value ** "*").get),

    jniCompile := Def.task {
      val log = streams.value.log

      // Make sure the output directory exists.
      binPath.value.mkdirs()

      val output = (binPath.value / sharedLibraryName(libraryName.value)).getPath
      val flags = ccOptions.value.distinct
      val sources = jniSourceFiles.value.map(_.getPath)

      val command = Seq(nativeCompiler.value, "-o", output) ++ flags ++ sources

      log.info(s"Compiling ${sources.size} C++ sources to ${binPath.value}")
      checkExitCode(command, log)
    }.dependsOn(javah)
     .tag(Tags.Compile, Tags.CPU)
     .value,

    javah := Def.task {
      val log = streams.value.log

      val classpath = (
        (dependencyClasspath in Compile).value.files ++
        Seq((classDirectory in Compile).value)
      ).mkString(sys.props("path.separator"))

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
    javaOptions in Test += s"-Djava.library.path=${binPath.value}",
    // Required in order to have a separate JVM to set Java options.
    fork in Test := true
  )
}

