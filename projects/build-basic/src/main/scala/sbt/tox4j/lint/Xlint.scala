package sbt.tox4j.lint

import sbt.Keys._
import sbt.{ AutoPlugin, PluginTrigger }

object Xlint extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override val projectSettings = Seq(
    scalacOptions ++= Seq("-Xlint", "-unchecked", "-feature", "-deprecation"),
    javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")
  )

}
