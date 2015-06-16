package sbt.tox4j

import sbt._
import sbt.tox4j.lint._

object Tox4jLibraryBuild extends Build {

  lazy val lint: Project = Project("lint", file("lint"))

  lazy val root: Project = Project("root", file("."),
    settings = Seq(
      Assembly,
      Benchmarking,
      CodeFormat,
      Jni,
      MakeScripts,
      ProtobufPlugin,
      Checkstyle,
      Findbugs,
      Scalastyle,
      Scapegoat,
      WartRemover,
      Xlint
    ).flatMap(_.moduleSettings)
  ).configs(ProtobufPlugin.Protobuf).dependsOn(lint)

}
