// General settings.
name          := "tox4j"
version       := "0.0.0-SNAPSHOT"

// Build dependencies.
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.json" % "json" % "20131018"
)

// Test dependencies.
libraryDependencies ++= Seq(
  "com.storm-enroute" %% "scalameter" % "0.7-SNAPSHOT",
  "junit" % "junit" % "4.12",
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12"
) map (_ % Test)

// Add ScalaMeter as test framework.
testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")


// JNI.
import sbt.tox4j.Jni.Keys._

packageDependencies ++= Seq(
  "protobuf-lite",
  "libtoxcore",
  "libtoxav",
  // Required, since toxav's pkg-config files are incomplete:
  "libsodium",
  "vpx"
)

// TODO: infer this (harder).
jniClasses := Seq(
  "im.tox.tox4j.impl.jni.ToxCryptoImpl$",
  "im.tox.tox4j.impl.jni.ToxAvJni",
  "im.tox.tox4j.impl.jni.ToxCoreJni"
)

// TODO: infer this (easy).
jniSourceFiles in Compile ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)

// Ignore generated proto sources in coverage.
ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*\\.proto\\..*"

// Add Scala linter.
resolvers += "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases"
addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1.9")
scalacOptions in Test += "-P:linter:disable:IdenticalStatements+VariableAssignedUnusedValue"

// Tox4j-specific style checkers.
addCompilerPlugin("im.tox" %% "linters" % "0.1-SNAPSHOT")

// Scalastyle configuration.
scalastyleConfig in Compile := (scalaSource in Compile).value / "scalastyle-config.xml"
scalastyleConfig in Test    := (scalaSource in Test   ).value / "scalastyle-config.xml"
