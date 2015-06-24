package sbt.tox4j.lint

import sbt.Keys._
import sbt.{Compile, _}
import sbt.tox4j.Tox4jBuildPlugin
import wartremover.Wart
import wartremover.WartRemover.autoImport._

object WartRemoverOverrides extends Tox4jBuildPlugin {

  object Keys

  private def custom(checker: String): Wart = {
    Wart.custom(s"im.tox.tox4j.lint.$checker")
  }

  // Enable checkstyle.
  override val moduleSettings = Seq(
    wartremoverErrors in (Compile, compile) ++= Seq(
      custom("OptionsClasses")
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
