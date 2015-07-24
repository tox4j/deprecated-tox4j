package sbt.tox4j.logic.jni

import java.io.{ File, PrintWriter }

import org.apache.commons.io.FilenameUtils
import sbt._

object Configure {

  object configLog extends AnyRef with ProcessLogger {
    val logFile = new PrintWriter("config.log")

    def buffer[T](f: => T): T = {
      val result = f
      logFile.flush()
      result
    }
    def error(s: => String) = logFile.println("[error] " + s)
    def info(s: => String) = logFile.println("[info] " + s)
  }

  /**
   * Extensions of source files.
   */
  private val cppExtensions = Seq(".cpp", ".cc", ".cxx", ".c")

  def isNativeSource(file: File): Boolean = {
    file.isFile && cppExtensions.exists(file.getName.toLowerCase.endsWith)
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
          Seq(compiler, sourceFile.getPath, "-o", targetFile.getPath, "-Werror") ++ flags !< configLog match {
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
      file(FilenameUtils.removeExtension(sourceFile.getName) + ".gcno").delete()
    }
  }

  def checkCcOptions(compiler: String, required: Boolean = false, code: String = "")(flags: Seq[String]*) = {
    findCcOptions(compiler, required, code)(flags: _*) match {
      case None        => Nil
      case Some(flags) => flags
    }
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

  private def findTool(toolName: String, toolchainPath: Option[File], code: String)(flags: Seq[String]*)(candidates: Seq[String]) = {
    mkToolchain(toolchainPath, candidates) find { cc =>
      try {
        val flagsOpt = Seq[String]() +: flags
        configLog.info(s"Trying $cc")
        Seq(cc, "--version") !< configLog == 0 && findCcOptions(cc, required = false, code)(flagsOpt: _*).isDefined
      } catch {
        case _: java.io.IOException => false
      }
    } getOrElse {
      sys.error(s"Could not find a working $toolName; tried: " + candidates)
    }
  }

  def findCc(toolchainPath: Option[File]) = {
    findTool(
      "C89 compiler",
      toolchainPath,
      """
    int foo(void);
    """
    )(
        Seq("-std=c89")
      )(
          sys.env.get("CC").toSeq ++ Seq("clang-3.6", "clang-3.5", "clang35", "gcc-4.9", "clang", "gcc", "cc")
        )
  }

  def findCxx(toolchainPath: Option[File]) = {
    findTool(
      "C++14 compiler",
      toolchainPath,
      """
    auto f = [](auto i) mutable { return i; };

    template<typename... Args>
    int bar (Args ...args) { return sizeof... (Args); }

    template<typename T>
    extern char const *x;

    template<>
    char const *x<int> = "int";

    template<typename... Args>
    auto foo (Args ...args) {
      return [&] { return bar (args...); };
    }
    """
    )(
        Seq("-std=c++14"),
        Seq("-std=c++1y")
      )(
          sys.env.get("CXX").toSeq ++ Seq("clang++-3.6", "clang++-3.5", "clang35++", "g++-4.9", "clang++", "g++", "c++")
        )
  }

}
