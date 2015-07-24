package sbt.tox4j.logic.jni

import sbt._

final case class BuildTool(
  name: String,
  command: String
)

object BuildTool {

  val Ninja = BuildTool("Ninja", "ninja")
  val Make = BuildTool("Unix Makefiles", "make")

  val tool = {
    Seq(Ninja, Make).foldLeft[Option[BuildTool]](None) { (found, next) =>
      found match {
        case Some(_) => found
        case None =>
          try {
            Seq(next.command, "--version") !< Configure.configLog
            Some(next)
          } catch {
            case _: java.io.IOException => None
          }
      }
    } getOrElse Make
  }

}
