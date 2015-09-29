package sbt.tox4j

import sbt.PluginTrigger
import scoverage.ScoverageSbtPlugin.ScoverageKeys._

/**
 * Default settings object for coverage. Requires 100% coverage on everything except generated protobuf code.
 */
object Coverage extends sbt.AutoPlugin {

  /**
   * This plugin is activated when all required plugins are present.
   * @return [[allRequirements]].
   */
  override def trigger: PluginTrigger = allRequirements

  /**
   * Require 100% coverage. Fail the test if coverage is not met.
   * @return A sequence of Settings.
   */
  override def projectSettings: Seq[sbt.Setting[_]] = Seq(
    // Require 100% test coverage.
    coverageMinimum := 100,
    coverageFailOnMinimum := true,

    // Ignore generated proto sources in coverage.
    coverageExcludedPackages := ".*\\.proto\\..*"
  )

}
