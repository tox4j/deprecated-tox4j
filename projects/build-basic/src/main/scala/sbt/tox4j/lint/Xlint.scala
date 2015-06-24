package sbt.tox4j.lint

import sbt.{PluginTrigger, AutoPlugin}
import sbt.Keys._

object Xlint extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override val projectSettings = Seq(
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-feature", "-deprecation"),
    javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")
  )

}
