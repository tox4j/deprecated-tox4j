package sbt.tox4j

import com.etsy.sbt.Checkstyle.CheckstyleTasks.checkstyle
import com.etsy.sbt.Checkstyle.checkstyleSettings
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.findbugs4sbt._
import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport._

object CodeStyle extends Tox4jBuildPlugin {

  object Keys {
    val checkstyleFatal = settingKey[Boolean]("Whether to fail the checkstyle task on Java code style violations.")
  }

  import Keys._

  private def custom(classpath: File): Seq[Wart] = {
    val pathFinder = ((classpath / "im" / "tox" / "tox4j" / "lint") ** "*.class") filter (!_.getName.contains('$'))
    pathFinder.get map { file =>
      val checker = file.getName.replace(".class", "")
      Wart.custom(s"im.tox.tox4j.lint.$checker")
    }
  }

  // Enable checkstyle.
  override val moduleSettings =
    Seq(
      scapegoatVerbose := false,
      scapegoatDisabledInspections := Seq(
        // Disable method name inspection, since we use things like name_=.
        "MethodNames",
        // This is simply wrong. Case classes can be extended, but it's a bad idea, so
        // we would like to make all of them final. WartRemover checks this.
        "RedundantFinalModifierOnCaseClass"
      ),
      scapegoatIgnoredFiles := Seq(".*/target/.*.scala", ".*/im/tox/tox4j/impl/jni/.*Impl\\.scala"),

      wartremoverClasspaths += (classDirectory in (Tox4jLibraryBuild.lint, Compile)).value.toURI.toString,
      wartremoverErrors in (Compile, compile) := Warts.allBut(
        Wart.Any,
        Wart.DefaultArguments,
        Wart.NonUnitStatements,
        Wart.Nothing,
        Wart.Throw,
        Wart.Var
      ) ++ custom((classDirectory in (Tox4jLibraryBuild.lint, Compile)).value),
      wartremoverErrors in (Test, compile) := Warts.allBut(
        Wart.Any,
        Wart.AsInstanceOf,
        Wart.DefaultArguments,
        Wart.IsInstanceOf,
        Wart.NonUnitStatements,
        Wart.Null,
        Wart.Throw,
        Wart.Var
      ),
      wartremoverExcluded := {
        val jni = (scalaSource in Compile).value / "im" / "tox" / "tox4j" / "impl" / "jni"
        val proto = (sourceManaged in Compile).value / "compiled_protobuf" / "im" / "tox" / "tox4j"

        // TODO: infer these
        val avProtos = Seq(
          "AudioBitRateStatus",
          "AudioReceiveFrame",
          "AvEvents",
          "Call",
          "CallState",
          "InternalFields_avProto",
          "VideoBitRateStatus",
          "VideoReceiveFrame"
        ).map(_ + ".scala").map(proto / "av" / "proto" / "Av" / _)

        val coreProtos = Seq(
          "Connection",
          "CoreEvents",
          "FileChunkRequest",
          "FileControl",
          "FileRecv",
          "FileRecvChunk",
          "FileRecvControl",
          "FriendConnectionStatus",
          "FriendLosslessPacket",
          "FriendLossyPacket",
          "FriendMessage",
          "FriendName",
          "FriendReadReceipt",
          "FriendRequest",
          "FriendStatus",
          "FriendStatusMessage",
          "FriendTyping",
          "InternalFields_coreProto",
          "MessageType",
          "SelfConnectionStatus",
          "UserStatus"
        ).map(_ + ".scala").map(proto / "core" / "proto" / "Core" / _)

        // TODO: also infer these
        Seq(
          jni / "ToxAvImpl.scala",
          jni / "ToxCoreImpl.scala"
        ) ++ avProtos ++ coreProtos
      },

      scalacOptions ++= Seq("-Xlint", "-unchecked", "-feature", "-deprecation"),
      javacOptions ++= Seq("-Xlint:deprecation"),

      // Fail if production code violates the coding style.
      checkstyleFatal in Compile := true,
      checkstyleFatal in Test := false

    ) ++ findbugsSettings ++ Seq(
        findbugsReportType := Some(ReportType.Html),
        findbugsExcludeFilters := Some(
          <FindBugsFilter>
            <Match>
              <Package name="im.tox.tox4j.av.proto.Av"/>
            </Match>
            <Match>
              <Package name="im.tox.tox4j.core.proto.Core"/>
            </Match>
          </FindBugsFilter>
        )

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
