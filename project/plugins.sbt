resolvers += Classpaths.sbtPluginReleases

// Scalastyle.
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

// Checkstyle.
addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "0.4.3")

// Coveralls.
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0.BETA1")

// Scala compiler options for SBT code.
scalacOptions ++= Seq("-feature", "-deprecation")

// Dependencies for SBT code.
libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"
