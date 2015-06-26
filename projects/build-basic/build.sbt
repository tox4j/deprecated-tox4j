organization  := "im.tox"
name          := "build-basic"

sbtPlugin := true

// https://github.com/scoverage/sbt-scoverage#highlighting
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := false
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 0

// Enable the plugins we want. Here we also need to explicitly apply AutoPlugins,
// because they are not loaded correctly by SBT in our bootstrap.
sbt.tox4j.lint.Checkstyle.moduleSettings
sbt.tox4j.lint.Scalastyle.moduleSettings
sbt.tox4j.Benchmarking.projectSettings
sbt.tox4j.CodeFormat.projectSettings
