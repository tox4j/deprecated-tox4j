organization  := "im.tox"
name          := "build-extra"

sbtPlugin := true

resolvers += Classpaths.sbtPluginReleases

// Import build-basic transitively.
addSbtPlugin("im.tox" % "build-basic" % "0.1-SNAPSHOT")

// Code style.
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.13")

// Test dependencies.
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4"
) map (_ % Test)

// https://github.com/scoverage/sbt-scoverage#highlighting
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := false
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 0

// Enable the plugins we want.
sbt.tox4j.lint.Checkstyle.moduleSettings
sbt.tox4j.lint.Scalastyle.moduleSettings
