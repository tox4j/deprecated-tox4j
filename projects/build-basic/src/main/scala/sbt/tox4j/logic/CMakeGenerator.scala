package sbt.tox4j.logic

import java.io.{ File, PrintWriter }

import sbt._

import scala.collection.mutable.ArrayBuffer

object CMakeGenerator {

  final case class GenerateFile(targetFile: File, lines: Seq[String]) {
    def apply(): File = {
      val out = new PrintWriter(targetFile)
      try {
        out.println(lines.mkString(System.lineSeparator))
      } finally {
        out.close()
      }

      targetFile
    }
  }

  def dependenciesFile(
    includes: Seq[File],
    packageDependencies: Seq[String],
    nativeSource: File,
    nativeTarget: File,
    managedNativeSource: File
  ): GenerateFile = {
    val targetFile = nativeTarget / "Dependencies.cmake"
    val lines = new ArrayBuffer[String]

    for (dir <- includes) {
      lines += s"include_directories($dir)"
    }

    if (packageDependencies.nonEmpty) {
      lines += "find_package(PkgConfig REQUIRED)"
    }
    packageDependencies foreach { pkg =>
      val PKG = pkg.toUpperCase.replace('-', '_')
      lines += s"pkg_check_modules($PKG REQUIRED $pkg)"
      lines += s"include_directories($${${PKG}_INCLUDE_DIRS})"
      lines += s"link_directories($${${PKG}_LIBRARY_DIRS})"
      lines += s"link_libraries($${${PKG}_LIBRARIES})"
    }

    Seq(nativeSource, nativeTarget, managedNativeSource) foreach { dir =>
      lines += s"include_directories($dir)"
    }

    GenerateFile(targetFile, lines)
  }

  def mainFile(
    binPath: File,
    libraryName: String,
    nativeTarget: File,
    jniSourceFiles: Seq[File]
  ): GenerateFile = {
    val targetFile = nativeTarget / "Main.cmake"
    val lines = new ArrayBuffer[String]

    lines += s"set(CMAKE_LIBRARY_OUTPUT_DIRECTORY $binPath)"
    lines += s"add_library($libraryName SHARED ${jniSourceFiles.mkString(" ")})"

    GenerateFile(targetFile, lines)
  }

}
