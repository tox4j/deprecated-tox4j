package sbt.tox4j

import sbt._

object Tox4jLibraryBuild extends Build {

  lazy val checkers = Project("checkers", file("checkers"))

  lazy val root = Project("root", file("."), settings =
    Assembly.moduleSettings ++
    Benchmarking.moduleSettings ++
    CodeFormat.moduleSettings ++
    CodeStyle.moduleSettings ++
    Jni.moduleSettings ++
    MakeScripts.moduleSettings ++
    ProtobufPlugin.moduleSettings
  ).configs(ProtobufPlugin.Protobuf).dependsOn(checkers)

}
