package sbt.tox4j.lint

import org.scalastyle.sbt.ScalastylePlugin.scalastyle
import sbt.tox4j.Tox4jBuildPlugin
import sbt.Keys._
import sbt._

object Scalastyle extends Tox4jBuildPlugin {

  object Keys {
    val packageCheckers = TaskKey[Unit]("packageCheckers")
  }

  import Keys._

  override val moduleSettings = Seq(
    packageCheckers := {
      val libDir = baseDirectory.value / "project" / "lib"
      val classDir = baseDirectory.value / "project" / "target" / "scala-2.10" / "sbt-0.13" / "classes"

      val classes = Seq(
        classOf[NopChecker]
      ).map(_.getName.replace('.', '/')).map(_ + ".class")

      libDir.mkdirs()
      Process(
        Seq("jar", "-cf", (libDir / "checkers.jar").getPath) ++ classes,
        classDir
      ) !< streams.value.log
    },

    scalastyle in Compile <<= (scalastyle in Compile).dependsOn(packageCheckers)
  )

}
