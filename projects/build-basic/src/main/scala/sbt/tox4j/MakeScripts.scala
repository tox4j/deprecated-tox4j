package sbt.tox4j

import sbt.Keys._
import sbt._

object MakeScripts extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object Keys {
    val makeScripts = TaskKey[Unit]("makeScripts")
  }

  import Keys._

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

  override val projectSettings: Seq[Setting[_]] = {
    makeScripts <<= (
      baseDirectory,
      javaOptions in Test,
      fullClasspath in Test,
      discoveredMainClasses in Test
    ) map mkrunTask
  }

}
