package sbt.tox4j

import sbt._

object Tox4jLibraryBuild extends Build {

  lazy val lint: Project = Project("lint", file("lint"))

  lazy val root: Project = Project("root", file("."),
    settings = Seq(
      Assembly,
      Benchmarking,
      CodeFormat,
      CodeStyle,
      Jni,
      MakeScripts,
      ProtobufPlugin
    ).flatMap(_.moduleSettings)
  ).configs(ProtobufPlugin.Protobuf).dependsOn(lint)

}
