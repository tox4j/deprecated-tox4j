package sbt.tox4j.lint

import sbt.Keys._
import sbt._
import sbt.tox4j.{Tox4jBuildPlugin, Tox4jLibraryBuild}
import wartremover.Wart
import wartremover.WartRemover.autoImport._

object WartRemover extends Tox4jBuildPlugin {

  object Keys

  private def custom(classpath: File): Seq[Wart] = {
    val pathFinder = ((classpath / "im" / "tox" / "tox4j" / "lint") ** "*.class") filter (!_.getName.contains('$'))
    pathFinder.get map { file =>
      val checker = file.getName.replace(".class", "")
      Wart.custom(s"im.tox.tox4j.lint.$checker")
    }
  }

  // Enable checkstyle.
  override val moduleSettings = Seq(
    wartremoverClasspaths += (classDirectory in (Tox4jLibraryBuild.lint, Compile)).value.toURI.toString,
    wartremoverErrors in (Compile, compile) := Warts.allBut(
      Wart.DefaultArguments,
      Wart.NonUnitStatements,
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
        "CallControl",
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
    }
  )

}
