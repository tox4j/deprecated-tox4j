package sbt.tox4j

import sbt._

import scala.language.postfixOps

abstract class Tox4jBuildPlugin extends Plugin {

  def moduleSettings: Seq[Setting[_]]

}
