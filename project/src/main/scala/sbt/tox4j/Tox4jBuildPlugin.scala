package sbt.tox4j

import sbt._

import scala.language.postfixOps

abstract class Tox4jBuildPlugin extends Plugin {

  def Keys: Any
  def moduleSettings: Seq[Setting[_]]

}
