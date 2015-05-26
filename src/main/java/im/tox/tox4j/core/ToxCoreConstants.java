package im.tox.tox4j.core;

public interface ToxCoreConstants {

  /**
   * The size of a Tox Public Key in bytes.
   */
  int PUBLIC_KEY_SIZE              = 32;

  /**
   * The size of a Tox Secret Key in bytes.
   */
  int SECRET_KEY_SIZE              = 32;

  /**
   * The size of a Tox address in bytes. Tox addresses are in the format
   * [Public Key (TOX_PUBLIC_KEY_SIZE bytes)][nospam (4 bytes)][checksum (2 bytes)].
   *
   * <p>
   * The checksum is computed over the Public Key and the nospam value. The first
   * byte is an XOR of all the odd bytes, the second byte is an XOR of all the
   * even bytes of the Public Key and nospam.
   */
  int ADDRESS_SIZE                = PUBLIC_KEY_SIZE + 4 + 2;

  /**
   * Maximum length of a nickname in bytes.
   */
  int MAX_NAME_LENGTH             = 128;

  /**
   * Maximum length of a status message in bytes.
   */
  int MAX_STATUS_MESSAGE_LENGTH   = 1007;

  /**
   * Maximum length of a friend request message in bytes.
   */
  int MAX_FRIEND_REQUEST_LENGTH   = 1016;

  /**
   * Maximum length of a single message after which it should be split.
   */
  int MAX_MESSAGE_LENGTH          = 1368;

  /**
   * Maximum size of custom packets. TODO: should be LENGTH?
   */
  int MAX_CUSTOM_PACKET_SIZE      = 1373;

  /**
   * Maximum file name length for file transfers.
   */
  int MAX_FILENAME_LENGTH         = 255;

  /**
   * Maximum hostname length. This is determined by calling <code>getconf HOST_NAME_MAX</code> on the console. The value
   * presented here is valid for most systems.
   */
  int MAX_HOSTNAME_LENGTH         = 255;

  /**
   * The number of bytes in a file id.
   */
  int FILE_ID_LENGTH              = 32;

  /**
   * Default port for HTTP proxies.
   */
  int DEFAULT_PROXY_PORT          = 8080;

  /**
   * Default start port for Tox UDP sockets.
   */
  int DEFAULT_START_PORT          = 33445;

  /**
   * Default end port for Tox UDP sockets.
   */
  int DEFAULT_END_PORT            = 33545;

  /**
   * Default port for Tox TCP relays. A value of 0 means disabled.
   */
  int DEFAULT_TCP_PORT            = 0;

}
