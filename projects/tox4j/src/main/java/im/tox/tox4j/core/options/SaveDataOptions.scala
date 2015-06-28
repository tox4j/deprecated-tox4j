package im.tox.tox4j.core.options

import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxSaveDataType

/**
 * The various kinds of save data that can be loaded by [[ToxCore.load]].
 */
object SaveDataOptions {

  /**
   * Base type for all save data kinds.
   */
  sealed trait Type {
    /**
     * The low level [[ToxSaveDataType]] enum to pass to [[ToxCore.load]].
     */
    def kind: ToxSaveDataType

    /**
     * Serialised save data. The format depends on [[kind]].
     */
    def data: Seq[Byte]
  }

  /**
   * No save data.
   */
  case object None extends Type {
    override def kind: ToxSaveDataType = ToxSaveDataType.NONE
    override def data: Seq[Byte] = Nil
  }

  /**
   * Full save data containing friend list, last seen DHT nodes, name, and all other information
   * contained within a Tox instance.
   */
  final case class ToxSave(data: Seq[Byte]) extends Type {
    override def kind: ToxSaveDataType = ToxSaveDataType.TOX_SAVE
  }

  /**
   * Minimal save data with just the secret key. The public key can be derived from it. Saving this
   * secret key, the friend list, name, and noSpam value is sufficient to restore the observable
   * behaviour of a Tox instance without the full save data in [[ToxSave]].
   */
  final case class SecretKey(data: Seq[Byte]) extends Type {
    override def kind: ToxSaveDataType = ToxSaveDataType.SECRET_KEY
  }

}
