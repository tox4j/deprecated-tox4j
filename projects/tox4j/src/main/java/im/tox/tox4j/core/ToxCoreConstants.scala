package im.tox.tox4j.core

object ToxCoreConstants {

  /**
   * The size of a Tox Public Key in bytes.
   */
  final val PUBLIC_KEY_SIZE = 32

  /**
   * The size of a Tox Secret Key in bytes.
   */
  final val SECRET_KEY_SIZE = 32

  /**
   * The size of a Tox address in bytes. Tox addresses are in the format
   * [Public Key (TOX_PUBLIC_KEY_SIZE bytes)][nospam (4 bytes)][checksum (2 bytes)].
   *
   * The checksum is computed over the Public Key and the nospam value. The first
   * byte is an XOR of all the odd bytes, the second byte is an XOR of all the
   * even bytes of the Public Key and nospam.
   */
  final val TOX_ADDRESS_SIZE = PUBLIC_KEY_SIZE + 4 + 2

  /**
   * Maximum length of a nickname in bytes.
   */
  final val MAX_NAME_LENGTH = 128

  /**
   * Maximum length of a status message in bytes.
   */
  final val MAX_STATUS_MESSAGE_LENGTH = 1007

  /**
   * Maximum length of a friend request message in bytes.
   */
  final val MAX_FRIEND_REQUEST_LENGTH = 1016

  /**
   * Maximum length of a single message after which it should be split.
   */
  final val MAX_MESSAGE_LENGTH = 1368

  /**
   * Maximum size of custom packets. TODO: should be LENGTH?
   */
  final val MAX_CUSTOM_PACKET_SIZE = 1373

  /**
   * Maximum file name length for file transfers.
   */
  final val MAX_FILENAME_LENGTH = 255

  /**
   * Maximum hostname length. This is determined by calling `getconf HOST_NAME_MAX` on the console. The value
   * presented here is valid for most systems.
   */
  final val MAX_HOSTNAME_LENGTH = 255

  /**
   * The number of bytes in a file id.
   */
  final val FILE_ID_LENGTH = 32

  /**
   * Default port for HTTP proxies.
   */
  final val DEFAULT_PROXY_PORT = 8080

  /**
   * Default start port for Tox UDP sockets.
   */
  final val DEFAULT_START_PORT = 33445

  /**
   * Default end port for Tox UDP sockets.
   */
  final val DEFAULT_END_PORT = 33545

  /**
   * Default port for Tox TCP relays. A value of 0 means disabled.
   */
  final val DEFAULT_TCP_PORT = 0

}
