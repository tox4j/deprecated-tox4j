import com.etsy.sbt.Checkstyle.CheckstyleTasks
import org.scalastyle.sbt.ScalastylePlugin.scalastyleConfig
import sbt.Keys._
import sbt._

import scala.language.postfixOps

object CodeStyle extends Plugin {

  object Keys {

    val checkstyleFatal = settingKey[Boolean]("Whether to fail the checkstyle task on Java code style violations.")

  }

  import Keys._

  // Enable checkstyle.
  override val settings =
    Seq(Compile, Test).map { config =>
      // Scalastyle configuration.
      scalastyleConfig in config := (scalaSource in config).value / "scalastyle-config.xml"
    } ++ Seq(
      // Fail if production code violates the coding style.
      checkstyleFatal in Compile := true,
      checkstyleFatal in Test    := false
    ) ++ com.etsy.sbt.Checkstyle.checkstyleSettings ++ Seq((Compile, ""), (Test, "-test")).map {
      case (config, suffix) =>
        // Checkstyle override to fail the build on errors.
        CheckstyleTasks.checkstyle in config := {
          val log = streams.value.log
          (CheckstyleTasks.checkstyle in config).value
          val resultFile = (target in config).value / s"checkstyle$suffix-report.xml"
          val results = scala.xml.XML.loadFile(resultFile)
          val errorFiles = results \\ "checkstyle" \\ "file"

          def errorFromXml(node: scala.xml.NodeSeq) = {
            val line = (node \ "@line").text
            val msg = (node \ "@message").text
            val source = (node \ "@source").text
            (line, msg, source)
          }

          def errorsFromXml(fileNode: scala.xml.NodeSeq) = {
            val name = (fileNode \ "@name").text
            val errors = (fileNode \\ "error") map errorFromXml
            errors map { case (line, error, source) => (name, line, error, source) }
          }

          val errors = errorFiles flatMap errorsFromXml

          if (errors.nonEmpty) {
            for (e <- errors) {
              log.error(s"${e._1}:${e._2}: ${e._3} (from ${e._4})")
            }
            val message = s"Checkstyle failed with ${errors.size} errors"
            if ((checkstyleFatal in config).value) {
              throw new RuntimeException(message)
            } else {
              log.warn(message)
            }
          } else {
            log.info("No errors from checkstyle")
          }
        }
    }

}
