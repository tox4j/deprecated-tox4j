package im.tox.tox4j.core.options

import im.tox.tox4j.core.ToxConstants
import im.tox.tox4j.core.enums.ToxSaveDataType

object SaveDataOptions {
  sealed trait Type {
    def kind: ToxSaveDataType
    def data: Array[Byte]
  }

  case object None extends Type {
    override def kind: ToxSaveDataType = ToxSaveDataType.NONE
    override def data: Array[Byte] = Array.ofDim(0)
  }

  final case class ToxSave(data: Array[Byte]) extends Type {
    require(data.nonEmpty)
    override def kind: ToxSaveDataType = ToxSaveDataType.TOX_SAVE
  }

  final case class SecretKey(data: Array[Byte]) extends Type {
    require(data.length == ToxConstants.SECRET_KEY_SIZE)
    override def kind: ToxSaveDataType = ToxSaveDataType.SECRET_KEY
  }
}
