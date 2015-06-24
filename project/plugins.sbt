resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("im.tox" % "sbt-tox4j" % "0.1-SNAPSHOT")

// Test coverage.
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0.BETA1")

// Scala compiler options for SBT code.
scalacOptions ++= Seq("-feature", "-deprecation")

// Dependencies for SBT code.
libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
)
