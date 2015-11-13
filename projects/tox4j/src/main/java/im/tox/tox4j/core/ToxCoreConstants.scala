package im.tox.tox4j.core

object ToxCoreConstants {

  /**
   * The size of a Tox Public Key in bytes.
   */
  final val PublicKeySize = 32

  /**
   * The size of a Tox Secret Key in bytes.
   */
  final val SecretKeySize = 32

  /**
   * The size of a Tox address in bytes. Tox addresses are in the format
   * [Public Key ([[PublicKeySize]] bytes)][nospam (4 bytes)][checksum (2 bytes)].
   *
   * The checksum is computed over the Public Key and the nospam value. The first
   * byte is an XOR of all the odd bytes, the second byte is an XOR of all the
   * even bytes of the Public Key and nospam.
   */
  final val AddressSize = PublicKeySize + 4 + 2

  /**
   * Maximum length of a nickname in bytes.
   */
  final val MaxNameLength = 128

  /**
   * Maximum length of a status message in bytes.
   */
  final val MaxStatusMessageLength = 1007

  /**
   * Maximum length of a friend request message in bytes.
   */
  final val MaxFriendRequestLength = 1016

  /**
   * Maximum length of a single message after which it should be split.
   */
  final val MaxMessageLength = 1372

  /**
   * Maximum size of custom packets. TODO: should be LENGTH?
   */
  final val MaxCustomPacketSize = 1373

  /**
   * Maximum file name length for file transfers.
   */
  final val MaxFilenameLength = 255

  /**
   * Maximum hostname length. This is determined by calling `getconf HOST_NAME_MAX` on the console. The value
   * presented here is valid for most systems.
   */
  final val MaxHostnameLength = 255

  /**
   * The number of bytes in a file id.
   */
  final val FileIdLength = 32

  /**
   * Default port for HTTP proxies.
   */
  final val DefaultProxyPort = 8080

  /**
   * Default start port for Tox UDP sockets.
   */
  final val DefaultStartPort = 33445

  /**
   * Default end port for Tox UDP sockets.
   */
  final val DefaultEndPort = DefaultStartPort + 100

  /**
   * Default port for Tox TCP relays. A value of 0 means disabled.
   */
  final val DefaultTcpPort = 0

}
