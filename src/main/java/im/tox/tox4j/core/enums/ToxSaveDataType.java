package im.tox.tox4j.core.enums;

import im.tox.tox4j.core.ToxCoreConstants;

/**
 * Type of savedata to create the Tox instance from.
 */
public enum ToxSaveDataType {
  /**
   * No savedata.
   */
  NONE,
  /**
   * Savedata is one that was obtained from {@link im.tox.tox4j.core.ToxCore#getSaveData}.
   */
  TOX_SAVE,
  /**
   * Savedata is a secret key of length {@link ToxCoreConstants#SECRET_KEY_SIZE}.
   */
  SECRET_KEY,
}
