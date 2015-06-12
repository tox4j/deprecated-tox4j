package sbt.tox4j

import sbt._

object Tox4jLibraryBuild extends Build {

  lazy val lint: Project = Project("lint", file("lint"))

  lazy val root: Project = Project("root", file("."), settings =
    Assembly.moduleSettings ++
    Benchmarking.moduleSettings ++
    CodeFormat.moduleSettings ++
    CodeStyle.moduleSettings ++
    Jni.moduleSettings ++
    MakeScripts.moduleSettings ++
    ProtobufPlugin.moduleSettings
  ).configs(ProtobufPlugin.Protobuf).dependsOn(lint)

}
