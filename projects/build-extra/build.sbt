organization  := "im.tox"
name          := "build-extra"

sbtPlugin := true

// https://github.com/scoverage/sbt-scoverage#highlighting
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := false
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 0

// Enable the plugins we want.
sbt.tox4j.lint.Checkstyle.moduleSettings
sbt.tox4j.lint.Scalastyle.moduleSettings
