// General settings
organization  := "im.tox"
name          := "tox4j"
version       := "0.0.0-SNAPSHOT"
scalaVersion  := "2.11.6"

// Mixed project.
compileOrder := CompileOrder.Mixed

scalaSource in Compile := (javaSource in Compile).value
scalaSource in Test    := (javaSource in Test   ).value

// Build dependencies
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.json" % "json" % "20131018"
)

// Test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4",
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12",
  "junit" % "junit" % "4.12"
) map (_ % Test)


// JNI
import Jni.Keys._

packageDependencies ++= Seq(
  "protobuf-lite",
  "libtoxcore",
  "libtoxav",
  // Required, since toxav's pkg-config files are incomplete:
  "libsodium",
  "vpx"
)

// Keep version in sync with libtoxcore.
versionSync := "libtoxcore"

// TODO: infer this (harder).
jniClasses := Seq(
  "im.tox.tox4j.impl.ToxAvJni",
  "im.tox.tox4j.impl.ToxCoreJni"
)

// TODO: infer this (easy).
jniSourceFiles in Compile ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)

// Current VM version.
val javaVersion = sys.props("java.specification.version")

// Java 1.6 for production code.
javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6")
scalacOptions in Compile += "-target:jvm-" + "1.6"

// Latest Java for test code.
javacOptions in Test ++= Seq("-source", javaVersion, "-target", javaVersion)
scalacOptions in Test += "-target:jvm-" + javaVersion

// Require 100% test coverage.
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 100
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true
