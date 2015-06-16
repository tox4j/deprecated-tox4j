package sbt.tox4j.lint

import com.etsy.sbt.Checkstyle.CheckstyleTasks.checkstyle
import com.etsy.sbt.Checkstyle.checkstyleSettings
import sbt.Keys._
import sbt._
import sbt.tox4j.Tox4jBuildPlugin

object Checkstyle extends Tox4jBuildPlugin {

  object Keys {
    val checkstyleFatal = settingKey[Boolean]("Whether to fail the checkstyle task on Java code style violations.")
  }

  import Keys._

  // Enable checkstyle.
  override val moduleSettings = Seq(
    // Fail if production code violates the coding style.
    checkstyleFatal in Compile := true,
    checkstyleFatal in Test := false

  ) ++ checkstyleSettings ++ Seq((Compile, ""), (Test, "-test")).map {
    case (config, suffix) =>
      // Checkstyle override to fail the build on errors.
      checkstyle in config := {
        (checkstyle in config).value

        val log = streams.value.log

        val errors = {
          val resultFile = (target in config).value / s"checkstyle$suffix-report.xml"
          val results = scala.xml.XML.loadFile(resultFile)
          val errorFiles = results \\ "checkstyle" \\ "file"
          errorFiles flatMap errorsFromXml
        }

        if (errors.nonEmpty) {
          for ((name, line, error, source) <- errors) {
            log.error(s"$name:$line: $error (from $source)")
          }

          val message = s"Checkstyle failed with ${errors.size} errors"
          if ((checkstyleFatal in config).value) {
            sys.error(message)
          } else {
            log.warn(message)
          }
        } else {
          log.info("No errors from checkstyle")
        }
      }
  }

  private def errorsFromXml(fileNode: scala.xml.NodeSeq) = {
    val name = (fileNode \ "@name").text
    val errors = (fileNode \\ "error") map errorFromXml
    errors map { case (line, error, source) => (name, line, error, source) }
  }

  private def errorFromXml(node: scala.xml.NodeSeq) = {
    val line = (node \ "@line").text
    val msg = (node \ "@message").text
    val source = (node \ "@source").text
    (line, msg, source)
  }

}
