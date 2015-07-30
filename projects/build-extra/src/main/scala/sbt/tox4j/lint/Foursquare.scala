package sbt.tox4j.lint

import sbt.Keys._
import sbt._
import sbt.tox4j.OptionalPlugin

object Foursquare extends OptionalPlugin {

  object Keys

  // Enable foursquare linter.
  override val moduleSettings = Seq(
    resolvers ++= Seq(
      "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases",
      Resolver.sonatypeRepo("snapshots")
    ),
    addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1-SNAPSHOT"),
    scalacOptions in Test += "-P:linter:disable:IdenticalStatements+VariableAssignedUnusedValue"
  )

}
