package im.tox.tox4j.core

import im.tox.tox4j.crypto.ToxCryptoConstants
import org.scalatest.FunSuite

final class ToxCoreConstantsTest extends FunSuite {

  test("MAX_FRIEND_REQUEST_LENGTH") {
    assert(ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH > 0)
    assert(ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH < ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE)
  }

  test("PUBLIC_KEY_SIZE") {
    assert(ToxCoreConstants.PUBLIC_KEY_SIZE == ToxCoreConstants.SECRET_KEY_SIZE)
    assert(ToxCoreConstants.PUBLIC_KEY_SIZE >= 32)
    assert(ToxCoreConstants.SECRET_KEY_SIZE >= 32)
  }

  test("MAX_NAME_LENGTH") {
    assert(ToxCoreConstants.MAX_NAME_LENGTH > 0)
    assert(ToxCoreConstants.MAX_NAME_LENGTH < ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE)
  }

  test("MAX_FILENAME_LENGTH") {
    assert(ToxCoreConstants.MAX_FILENAME_LENGTH > 0)
    assert(ToxCoreConstants.MAX_FILENAME_LENGTH < ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE)
  }

  test("MAX_CUSTOM_PACKET_SIZE") {
    assert(ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE > 0)
    assert(ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE <= 1500) // Ethernet MTU
  }

  test("MAX_HOSTNAME_LENGTH") {
    assert(ToxCoreConstants.MAX_HOSTNAME_LENGTH > 0)
  }

  test("TOX_ADDRESS_SIZE") {
    assert(ToxCoreConstants.ADDRESS_SIZE >= ToxCoreConstants.ADDRESS_SIZE)
  }

  test("FILE_ID_LENGTH") {
    assert(ToxCoreConstants.FILE_ID_LENGTH >= ToxCryptoConstants.HASH_LENGTH)
  }

  test("DEFAULT_END_PORT") {
    assert(ToxCoreConstants.DEFAULT_END_PORT >= 1)
    assert(ToxCoreConstants.DEFAULT_END_PORT <= 65535)
  }

  test("MAX_MESSAGE_LENGTH") {
    assert(ToxCoreConstants.MAX_MESSAGE_LENGTH > 0)
    assert(ToxCoreConstants.MAX_MESSAGE_LENGTH < ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE)
  }

  test("MAX_STATUS_MESSAGE_LENGTH") {
    assert(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH > 0)
    assert(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH < ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE)
  }

  test("DEFAULT_TCP_PORT") {
    assert(ToxCoreConstants.DEFAULT_TCP_PORT >= 0) // 0 means no TCP
    assert(ToxCoreConstants.DEFAULT_TCP_PORT <= 65535)
  }

  test("DEFAULT_START_PORT") {
    assert(ToxCoreConstants.DEFAULT_START_PORT >= 1)
    assert(ToxCoreConstants.DEFAULT_START_PORT <= 65535)
  }

  test("DEFAULT_PROXY_PORT") {
    assert(ToxCoreConstants.DEFAULT_PROXY_PORT >= 1)
    assert(ToxCoreConstants.DEFAULT_PROXY_PORT <= 65535)
  }

}
