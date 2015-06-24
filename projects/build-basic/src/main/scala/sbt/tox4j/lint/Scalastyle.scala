package sbt.tox4j.lint

import org.scalastyle.sbt.ScalastylePlugin.scalastyleConfigUrl
import sbt._
import sbt.tox4j.OptionalPlugin

object Scalastyle extends OptionalPlugin {
  object Keys

  private val config = Some(getClass.getResource("scalastyle-config.xml"))

  override val moduleSettings = Seq(
    scalastyleConfigUrl := config,
    scalastyleConfigUrl in Test := config
  )
}
