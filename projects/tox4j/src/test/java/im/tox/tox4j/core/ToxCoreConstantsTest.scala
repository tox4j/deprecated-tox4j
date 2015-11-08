package im.tox.tox4j.core

import im.tox.tox4j.crypto.ToxCryptoConstants
import org.scalatest.FunSuite

final class ToxCoreConstantsTest extends FunSuite {

  test("MAX_FRIEND_REQUEST_LENGTH") {
    assert(ToxCoreConstants.MaxFriendRequestLength > 0)
    assert(ToxCoreConstants.MaxFriendRequestLength < ToxCoreConstants.MaxCustomPacketSize)
  }

  test("PUBLIC_KEY_SIZE") {
    assert(ToxCoreConstants.PublicKeySize == ToxCoreConstants.SecretKeySize)
    assert(ToxCoreConstants.PublicKeySize >= 32)
    assert(ToxCoreConstants.SecretKeySize >= 32)
  }

  test("MAX_NAME_LENGTH") {
    assert(ToxCoreConstants.MaxNameLength > 0)
    assert(ToxCoreConstants.MaxNameLength < ToxCoreConstants.MaxCustomPacketSize)
  }

  test("MAX_FILENAME_LENGTH") {
    assert(ToxCoreConstants.MaxFilenameLength > 0)
    assert(ToxCoreConstants.MaxFilenameLength < ToxCoreConstants.MaxCustomPacketSize)
  }

  test("MAX_CUSTOM_PACKET_SIZE") {
    assert(ToxCoreConstants.MaxCustomPacketSize > 0)
    assert(ToxCoreConstants.MaxCustomPacketSize <= 1500) // Ethernet MTU
  }

  test("MAX_HOSTNAME_LENGTH") {
    assert(ToxCoreConstants.MaxHostnameLength > 0)
  }

  test("TOX_ADDRESS_SIZE") {
    assert(ToxCoreConstants.AddressSize >= ToxCoreConstants.AddressSize)
  }

  test("FILE_ID_LENGTH") {
    assert(ToxCoreConstants.FileIdLength >= ToxCryptoConstants.HashLength)
  }

  test("DEFAULT_END_PORT") {
    assert(ToxCoreConstants.DefaultEndPort >= 1)
    assert(ToxCoreConstants.DefaultEndPort <= 65535)
  }

  test("MAX_MESSAGE_LENGTH") {
    assert(ToxCoreConstants.MaxMessageLength > 0)
    assert(ToxCoreConstants.MaxMessageLength < ToxCoreConstants.MaxCustomPacketSize)
  }

  test("MAX_STATUS_MESSAGE_LENGTH") {
    assert(ToxCoreConstants.MaxStatusMessageLength > 0)
    assert(ToxCoreConstants.MaxStatusMessageLength < ToxCoreConstants.MaxCustomPacketSize)
  }

  test("DEFAULT_TCP_PORT") {
    assert(ToxCoreConstants.DefaultTcpPort >= 0) // 0 means no TCP
    assert(ToxCoreConstants.DefaultTcpPort <= 65535)
  }

  test("DEFAULT_START_PORT") {
    assert(ToxCoreConstants.DefaultStartPort >= 1)
    assert(ToxCoreConstants.DefaultStartPort <= 65535)
  }

  test("DEFAULT_PROXY_PORT") {
    assert(ToxCoreConstants.DefaultProxyPort >= 1)
    assert(ToxCoreConstants.DefaultProxyPort <= 65535)
  }

}
