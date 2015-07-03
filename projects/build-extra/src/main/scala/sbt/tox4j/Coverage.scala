package sbt.tox4j

import sbt.PluginTrigger
import scoverage.ScoverageSbtPlugin.ScoverageKeys._

object Coverage extends sbt.AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[sbt.Setting[_]] = Seq(
    // Require 100% test coverage.
    coverageMinimum := 100,
    coverageFailOnMinimum := true,

    // Ignore generated proto sources in coverage.
    coverageExcludedPackages := ".*\\.proto\\..*"
  )

}
