package sbt.tox4j

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import sbt.Keys._
import sbt._

object Benchmarking extends Plugin {

  object Keys {
    val benchmarkMachine = settingKey[String]("Name of the machine running the benchmark.")

    val benchmark = taskKey[Unit]("Run all benchmarks and upload the results.")
    val uploadBenchmarkResults = taskKey[Unit]("Upload bench mark results.")
  }

  import Keys._

  override val settings = Seq[Setting[_]](
    benchmarkMachine := "travis",

    uploadBenchmarkResults := {
      upload(streams.value.log, baseDirectory.value, benchmarkMachine.value, target.value)
    },

    benchmark := {
      (testOnly in Test).toTask(" *Bench").value
      upload(streams.value.log, baseDirectory.value, benchmarkMachine.value, target.value)
    }
  )

  private def upload(log: Logger, baseDirectory: File, benchmarkMachine: String, target: File) = {
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
