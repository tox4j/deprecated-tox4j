package sbt.tox4j

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import sbt.Keys._
import sbt._

object Benchmarking extends Plugin {

  val Benchmark = config("benchmark")

  object Keys {
    val machine = settingKey[String]("Name of the machine running the benchmark.")

    val upload = taskKey[Unit]("Upload bench mark results.")
    val benchmark = taskKey[Unit]("Run all benchmarks and upload the results.")
  }

  import Keys._

  override val settings = inConfig(Benchmark)(Seq(
    machine := "travis",

    run := {
      (testOnly in Test).toTask(" *Bench").value
    },

    upload := {
      uploadResults(streams.value.log, baseDirectory.value, machine.value, target.value)
    },

    benchmark := {
      (testOnly in Test).toTask(" *Bench").value
      uploadResults(streams.value.log, baseDirectory.value, machine.value, target.value)
    }
  ))

  private def uploadResults(log: Logger, baseDirectory: File, benchmarkMachine: String, target: File) = {
    val webDir = baseDirectory / ".web" / "report" / benchmarkMachine

    def dataJs(dir: File) = {
      dir / "js" / "ScalaMeter" / "data.js"
    }

    def git(args: String*) = {
      Process("git" +: args, webDir) ! log
    }

    if (webDir.exists) {
      git("config", "push.default", "simple")

      git("pull")
      IO.copyFile(dataJs(target / "benchmarks" / "report"), dataJs(webDir))
      git("commit", "-a", "-m", s"Added benchmark results of $benchmarkMachine at $now")
      git("push")
    }
  }

  private def now: String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm")
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    dateFormat.format(new Date)
  }

}
