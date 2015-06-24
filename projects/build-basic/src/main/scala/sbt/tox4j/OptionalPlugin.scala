package sbt.tox4j

import sbt._

// scalastyle:off
abstract class OptionalPlugin extends Plugin {
  def Keys: Any
  def moduleSettings: Seq[Setting[_]]
}
