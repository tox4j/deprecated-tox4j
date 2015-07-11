package im.tox.tox4j.impl.jni.codegen

import im.tox.tox4j.core.enums._

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Nothing"))
final class JniEnums {

  val enumTypes = Seq[Array[_ <: Enum[_]]](
    ToxConnection.values,
    ToxFileControl.values,
    ToxMessageType.values,
    ToxProxyType.values,
    ToxSaveDataType.values,
    ToxUserStatus.values
  )

}
