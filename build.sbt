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
  "junit" % "junit" % "4.12"
  //"org.easetech" % "easytest" % "0.6.3"
) map (_ % Test)

// JNI
import Jni.Keys._

packageDependencies ++= Seq(
  "protobuf-lite",
  "libtoxcore",
  "libtoxav",
  // Required, since toxcore's pkg-config files are incomplete:
  "libsodium",
  "vpx"
)

jniClasses := Seq(
  "im.tox.tox4j.ToxAvImpl",
  "im.tox.tox4j.ToxCoreImpl"
)

jniSourceFiles ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)
