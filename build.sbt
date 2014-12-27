// General settings
organization  := "im.tox"
name          := "tox4j"
version       := "0.0.0"
scalaVersion  := "2.11.4"

// Compile Java code first.
compileOrder := CompileOrder.JavaThenScala

// Test dependencies
libraryDependencies ++= Seq(
  "org.json" % "json" % "20131018",

  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % Test,
  "junit" % "junit" % "4.12" % Test
)

// JNI
import Jni.Keys._

Jni.settings

libraryName := "libtox4j"

gccFlags ++= Seq("-I/srv/pippijn/code/git/tox/tox4j/src/main/native")

jniClasses := Seq(
  "im.tox.tox4j.ToxAvImpl",
  "im.tox.tox4j.ToxCoreImpl"
)

packageDependencies ++= Seq(
  "protobuf-lite",
  "libtoxcore",
  "libtoxav"
)

// Protobuf
ProtobufPlugin.settings

jniSourceFiles ++= Seq(
  managedNativeSourceDirectories.value / "compiled_protobuf" / "Av.pb.cc",
  managedNativeSourceDirectories.value / "compiled_protobuf" / "Core.pb.cc"
)
