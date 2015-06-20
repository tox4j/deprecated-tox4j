package sbt.tox4j.lint

import sbt.Keys._
import sbt.tox4j.Tox4jBuildPlugin

object Xlint extends Tox4jBuildPlugin {

  object Keys

  override val moduleSettings = Seq(
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-feature", "-deprecation"),
    javacOptions ++= Seq("-Xlint:deprecation")
  )

}
