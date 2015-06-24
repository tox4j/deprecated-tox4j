package sbt.tox4j.lint

import sbt.Keys._
import sbt._
import sbt.tox4j.OptionalPlugin

object Foursquare extends OptionalPlugin {

  object Keys

  // Enable foursquare linter.
  override val moduleSettings = Seq(
    resolvers += "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases",
    addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1.9"),
    scalacOptions in Test += "-P:linter:disable:IdenticalStatements+VariableAssignedUnusedValue"
  )

}
