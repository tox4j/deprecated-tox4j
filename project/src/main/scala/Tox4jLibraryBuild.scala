package src.main.scala

import sbt.Keys._
import sbt._

object Tox4jLibraryBuild extends Build {

  private val mkrun = TaskKey[Unit]("mkrun")

  private def classBaseName(s: String): String = {
    val lastDot = s.lastIndexOf('.')
    if (lastDot == -1) {
      s
    } else {
      s.substring(lastDot + 1)
    }
  }

  private def mkrunTask(base: File, opts: Seq[String], cp: Classpath, mains: Seq[String]) = {
    val template = """#!/usr/bin/env perl
exec "java", "%s", "-classpath", "%s", "%s", @ARGV
                   """
    for (main <- mains) {
      val contents = template.format(opts.mkString("\", \""), cp.files.absString, main)
      val out = base / "bin" / classBaseName(main)
      IO.write(out, contents)
      out.setExecutable(true)
    }
  }

  lazy val root = Project("root", file("."), settings =
    mkrun <<= (
      baseDirectory,
      javaOptions in Test,
      fullClasspath in Test,
      discoveredMainClasses in Test
    ) map mkrunTask
  ).configs(ProtobufPlugin.Protobuf)

}
