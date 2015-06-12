package sbt.tox4j

import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin

object Tox4jBuildSettings extends Plugin {

  // Current VM version.
  val javaVersion = sys.props("java.specification.version")

  override val settings = Seq(
    // General settings.
    organization  := "im.tox",
    scalaVersion  := "2.11.6",

    scalaSource in Compile := (javaSource in Compile).value,
    scalaSource in Test    := (javaSource in Test   ).value,

    // Mixed project.
    compileOrder := CompileOrder.Mixed,

    // Java 1.6 for production code.
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6"),
    scalacOptions in Compile += "-target:jvm-" + "1.6",

    // Latest Java for test code.
    javacOptions in Test ++= Seq("-source", javaVersion, "-target", javaVersion),
    scalacOptions in Test += "-target:jvm-" + javaVersion,

    // Require 100% test coverage.
    ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 100,
    ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true,

    // Snapshot repository.
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

}
