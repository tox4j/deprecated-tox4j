// General settings.
organization  := "im.tox"
name          := "tox4j"
scalaVersion  := "2.11.6"

// Enable the plugins we want.
import sbt.tox4j._
import sbt.tox4j.lint._

// Build plugins.
Assembly.moduleSettings
Benchmarking.moduleSettings
Jni.moduleSettings
ProtobufJni.moduleSettings

/******************************************************************************
 * Dependencies
 ******************************************************************************/

// Snapshot and linter repository.
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

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

// TODO: infer this (easy).
jniSourceFiles in Compile ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)

/******************************************************************************
 * Other settings and plugin configuration.
 ******************************************************************************/

// Mixed project.
compileOrder := CompileOrder.Mixed
scalaSource in Compile := (javaSource in Compile).value
scalaSource in Test    := (javaSource in Test   ).value

// Lint plugins.
Checkstyle.moduleSettings
Findbugs.moduleSettings
Foursquare.moduleSettings
Scalastyle.moduleSettings

// Local overrides for linters.
WartRemoverOverrides.moduleSettings

// TODO(iphydf): Require less test coverage for now, until ToxAv is tested.
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 78

// Tox4j-specific style checkers.
addCompilerPlugin("im.tox" %% "linters" % "0.1-SNAPSHOT")

// Override Scalastyle configuration for test.
scalastyleConfigUrl in Test := None
scalastyleConfig in Test := (scalaSource in Test).value / "scalastyle-config.xml"

// Current VM version.
val javaVersion = sys.props("java.specification.version")

// Java 1.6 for production code.
javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6")
scalacOptions in Compile += "-target:jvm-" + "1.6"

// Latest Java for test code.
javacOptions in Test ++= Seq("-source", javaVersion, "-target", javaVersion)
scalacOptions in Test += "-target:jvm-" + javaVersion
