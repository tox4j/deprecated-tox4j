package src.main.scala

import com.etsy.sbt.Checkstyle.CheckstyleTasks
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.findbugs4sbt._
import org.scalastyle.sbt.ScalastylePlugin.scalastyleConfig
import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._

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

      scapegoatDisabledInspections := Seq(
        // Disable method name inspection, since we use things like name_=.
        "MethodNames",
        // This is simply wrong. Case classes can be extended, but it's a bad idea, so
        // we would like to make all of them final. WartRemover checks this.
        "RedundantFinalModifierOnCaseClass"
      ),
      scapegoatIgnoredFiles := Seq(".*/target/.*.scala", ".*/im/tox/tox4j/impl/.*Impl\\.scala"),

      wartremoverErrors in (Compile, compile) := Warts.allBut(
        Wart.DefaultArguments,
        Wart.NonUnitStatements,
        Wart.Var
      ),
      wartremoverErrors in (Test, compile) := Warts.allBut(
        Wart.Any,
        Wart.AsInstanceOf,
        Wart.DefaultArguments,
        Wart.NonUnitStatements,
        Wart.Null,
        Wart.Throw,
        Wart.Var
      ),
      wartremoverExcluded := Seq(
        baseDirectory.value / "src" / "main" / "java" / "im" / "tox" / "tox4j" / "impl" / "ToxAvImpl.scala",
        baseDirectory.value / "src" / "main" / "java" / "im" / "tox" / "tox4j" / "impl" / "ToxCoreImpl.scala",
        baseDirectory.value / "target/scala-2.11/src_managed/main/compiled_protobuf/im/tox/tox4j/av/proto/Av.scala",
        baseDirectory.value / "target/scala-2.11/src_managed/main/compiled_protobuf/im/tox/tox4j/core/proto/Core.scala"
      ),

      scalacOptions ++= Seq("-Xlint", "-unchecked", "-feature", "-deprecation"),

      // Fail if production code violates the coding style.
      checkstyleFatal in Compile := true,
      checkstyleFatal in Test := false

    ) ++ findbugsSettings ++ Seq(
        findbugsReportType := Some(ReportType.Html),
        findbugsExcludeFilters := Some(
          <FindBugsFilter>
            <Match>
              <Package name="im.tox.tox4j.av.proto"/>
            </Match>
            <Match>
              <Package name="im.tox.tox4j.core.proto"/>
            </Match>
          </FindBugsFilter>
        )

      ) ++ com.etsy.sbt.Checkstyle.checkstyleSettings ++ Seq((Compile, ""), (Test, "-test")).map {
          case (config, suffix) =>
            // Checkstyle override to fail the build on errors.
            CheckstyleTasks.checkstyle in config := {
              (CheckstyleTasks.checkstyle in config).value

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
