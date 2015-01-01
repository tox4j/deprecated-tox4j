// General settings
organization  := "im.tox"
name          := "tox4j"
version       := "0.0.0"
scalaVersion  := "2.11.4"

// Compile Java code first.
compileOrder := CompileOrder.JavaThenScala

// Test dependencies
libraryDependencies ++= Seq(
  "org.json" % "json" % "20131018"
)

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11",
  "org.scalatest" %% "scalatest" % "2.2.1",
  "org.hamcrest" % "hamcrest-all" % "1.3",
  "junit" % "junit" % "4.12"
  //"org.easetech" % "easytest" % "0.6.3"
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
  "im.tox.tox4j.ToxAvImpl",
  "im.tox.tox4j.ToxCoreImpl"
)

// TODO: infer this (easy).
jniSourceFiles ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)

// Current VM version.
val javaVersion = sys.props("java.specification.version")

// Java 1.6 for production code.
javacOptions in Compile ++= Seq("-source", "1.6", "-target", javaVersion)
scalacOptions in Compile += "-target:jvm-" + "1.6"

// Latest Java for test code.
javacOptions in Test ++= Seq("-source", javaVersion, "-target", javaVersion)
scalacOptions in Compile += "-target:jvm-" + javaVersion
