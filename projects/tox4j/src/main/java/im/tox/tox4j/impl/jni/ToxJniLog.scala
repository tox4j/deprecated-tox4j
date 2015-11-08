package im.tox.tox4j.impl.jni

import im.tox.tox4j.impl.jni.proto.ProtoLog.{Member, Value, JniLogEntry, JniLog}

object ToxJniLog extends (() => JniLog) {

  def enable(): Unit = ToxCoreJni.tox4jSetLogging(true)
  def disable(): Unit = ToxCoreJni.tox4jSetLogging(false)

  def apply(): JniLog = {
    Option(ToxCoreJni.tox4jLastLog()).map(JniLog.parseFrom).getOrElse(JniLog.defaultInstance)
  }

  def toString(log: JniLog): String = {
    log.entries.map(toString).mkString("\n")
  }

  def toString(entry: JniLogEntry): String = {
    s"${entry.name}(${entry.arguments.map(toString).mkString(", ")}) = " +
      s"${toString(entry.result)} [${entry.elapsedTime} µs]" // scalastyle:ignore non.ascii.character.disallowed
  }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.IsInstanceOf", "org.brianmckenna.wartremover.warts.AsInstanceOf"))
  def toString(value: Value): String = {
    value match {
      case Value(Some(uint32), None, None, Nil) => uint32.toString
      case Value(None, Some(string), None, Nil) => string
      case Value(None, None, Some(bytes), Nil)  => s"bytes[${bytes.size}]"
      case Value(None, None, None, Nil)         => "void"
      case Value(None, None, None, members)     => s"{${members.map(toString).mkString("; ")}}"
      case _                                    => assert(false, "Invalid oneof message"); "<error>"
    }
  }

  def toString(member: Member): String = {
    s"${member.name}=${toString(member.value)}"
  }

}
