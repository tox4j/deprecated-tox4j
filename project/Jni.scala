import sbt._
import Keys._

import scala.language.postfixOps

object Jni {
  val jniConfig = config("native")

  object Keys {

    // tasks

    lazy val jni = taskKey[Unit]("Run jni build")
    lazy val jniCompile = taskKey[Unit]("Compiles jni sources using gcc")
    lazy val javah = taskKey[Unit]("Builds jni sources")

    // settings

    lazy val packageDependencies = settingKey[Seq[String]]("Dependencies from pkg-config")

    lazy val nativeCompiler = settingKey[String]("Compiler to use. Defaults to gcc")
    lazy val cppExtensions = settingKey[Seq[String]]("Extensions of source files")
    lazy val jniClasses = settingKey[Seq[String]]("Classes with native methods")
    lazy val headersPath = settingKey[File]("Generated JNI headers")
    lazy val extraHeadersPath = settingKey[File]("Extra generated headers")
    lazy val binPath = settingKey[File]("Shared libraries produced by JNI")
    lazy val nativeSource = settingKey[File]("JNI native sources")
    lazy val managedNativeSourceDirectories = settingKey[File]("Generated JNI native sources")
    lazy val includes = settingKey[Seq[String]]("Compiler includes settings")
    lazy val jniSourceFiles = settingKey[Seq[File]]("Jni source files")
    lazy val gccFlags = settingKey[Seq[String]]("Flags to be passed to gcc")
    lazy val libraryName = settingKey[String]("Shared library produced by JNI")
    lazy val jreIncludes = settingKey[Seq[String]]("Includes for jni")
    lazy val jdkHome = settingKey[Option[File]]("Used to find jre include files for JNI")
    lazy val cpp11 = settingKey[Boolean]("Whether to pass the cpp11 flag to the compiler")

  }

  import Keys._


  def jreIncludeFolder = {
    System.getProperty("os.name") match {
      case "Linux" => "linux"
      case "Mac OS X" => "darwin"
      case  _ => throw new Exception("Cannot determine os name for JRE include folder.")
    }
  }

  def withExtensions(files: Seq[File], extensions: Seq[String]) = {
    files.filter { file =>
      file.isFile && extensions.exists(file.getName.toLowerCase.endsWith)
    }
  }

  def pkgConfig(pkgs: Seq[String]) = {
    pkgs match {
      case Nil =>
        Nil
      case pkgs =>
        val result = (Process(Seq("pkg-config", "--cflags", "--libs") ++ pkgs) !!)
        result.split(" ").toSeq
    }
  }

  val settings = inConfig(jniConfig)(Seq(

    jdkHome := {
      val home = file(System.getProperty("java.home"))
      if (home.exists)
        Some(home)
      else
        None
    },

    jreIncludes := {
      jdkHome.value.fold(Seq.empty[String]) { home =>
        val absHome = home.getAbsolutePath
        // in a typical installation, jdk files are one directory above the location of the jre set in 'java.home'
        Seq(s"include", s"include/$jreIncludeFolder").map(file => s"-I${absHome}/../$file")
      }
    },

    includes := Nil,

    includes ++= Seq(
      s"-I${headersPath.value}",
      s"-I${extraHeadersPath.value}",
      s"-I${nativeSource.value}",
      "-I/usr/include",
      "-L/usr/local/include"
    ),
  
    includes ++= jreIncludes.value,
    
    includes ++= pkgConfig(packageDependencies.value)

  )) ++ Seq(

    packageDependencies := Nil,

    binPath := (target in Compile).value / "cpp" / "bin",
    headersPath := (target in Compile).value / "cpp" / "include",
    nativeSource := sourceDirectory.value / "main" / "cpp",
    managedNativeSourceDirectories := (target in Compile).value / "cpp" / "source",

    nativeCompiler := "clang++",
    jniClasses := Seq.empty,
    cpp11 := true,
    gccFlags := Seq(
      "-shared",
      "-fPIC"
    ) ++ (if (cpp11.value) Seq("-std=c++11") else Seq.empty)
      ++ (includes in jniConfig).value,
    extraHeadersPath := file("."),
    cppExtensions := Seq(".c", ".cpp", ".cc", ".cxx"),
    jniSourceFiles := withExtensions((nativeSource.value ** "*").get, cppExtensions.value),
    jniCompile := Def.task {
      val log = streams.value.log
      val mkBinDir = s"mkdir -p ${binPath.value}" 
      log.info(mkBinDir)
      mkBinDir ! log
      val sources = jniSourceFiles.value.mkString(" ")
      val flags = gccFlags.value.mkString(" ")
      //TODO: .so for linux, .dylib for mac
      val command = s"${nativeCompiler.value} $flags -o ${binPath.value}/${libraryName.value}.so $sources"
      log.info(command)
      Process(command, binPath.value) ! (log)
    }.dependsOn(javah)
     .tag(Tags.Compile, Tags.CPU)
     .value,

    javah := Def.task {
      val log = streams.value.log
      val dependencies = (dependencyClasspath in Compile).value.files ++ Seq((classDirectory in Compile).value)
      log.info("Running javah to generate JNI headers")
      val classpath = dependencies.mkString(sys.props("path.separator"))
      val javahCommand = Process(
        Seq(
          "javah",
          "-d", headersPath.value.absolutePath,
          "-classpath", classpath
        ) ++ jniClasses.value
      )
      javahCommand ! log
    }.dependsOn(compile in Compile)
     .tag(Tags.Compile, Tags.CPU)
     .value,

    compile <<= (compile in Compile, jniCompile).map((result, _) => result),

    cleanFiles ++= Seq( 
      binPath.value,
      headersPath.value
    ),

    // Make shared lib available at runtime. Must be used with forked jvm to work.
    javaOptions in Test += s"-Djava.library.path=${binPath.value}",
    //required in order to have a separate jvm to set java options
    fork in Test := true
  )
}

