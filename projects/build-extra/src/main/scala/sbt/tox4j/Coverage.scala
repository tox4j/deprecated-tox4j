package sbt.tox4j

import scoverage.ScoverageSbtPlugin.ScoverageKeys._

object Coverage extends sbt.Plugin {

  override val settings = Seq(
    // Require 100% test coverage.
    coverageMinimum := 100,
    coverageFailOnMinimum := true,

    // Ignore generated proto sources in coverage.
    coverageExcludedPackages := ".*\\.proto\\..*"
  )

}
