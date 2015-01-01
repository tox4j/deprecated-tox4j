#pragma once

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#include <tox/core_compat.h>

#ifdef __cplusplus
extern "C" {
#endif

/** \page core Public core API for Tox clients.
 *
 * Every function that can fail takes a function-specific error code pointer
 * that can be used to diagnose problems with the Tox state or the function
 * arguments. The error code pointer can be NULL, which does not influence the
 * function's behaviour, but can be done if the reason for failure is irrelevant
 * to the client.
 *
 * The exception to this rule are simple allocation functions whose only failure
 * mode is allocation failure. They return NULL in that case, and do not set an
 * error code.
 *
 * Every error code type has an OK value to which functions will set their error
 * code value on success. Clients can keep their error code uninitialised before
 * passing it to a function. The library guarantees that after returning, the
 * value pointed to by the error code pointer has been initialised.
 *
 * Functions with pointer parameters often have a NULL error code, meaning they
 * could not perform any operation, because one of the required parameters was
 * NULL. Some functions operate correctly or are defined as effectless on NULL.
 *
 * Some functions additionally return a value outside their
 * return type domain, or a bool containing true on success and false on
 * failure.
 *
 * All functions that take a Tox instance pointer will cause undefined behaviour
 * when passed a NULL Tox pointer.
 *
 * All integer values are expected in host byte order.
 *
 * Functions with parameters with enum types cause unspecified behaviour if the
 * enumeration value is outside the valid range of the type. If possible, the
 * function will try to use a sane default, but there will be no error code,
 * and one possible action for the function to take is to have no effect.
 */

/** \subsection events Events and callbacks
 *
 * Events are handled by callbacks. One callback can be registered per event.
 * All events have a callback function type named `tox_${event}_cb` and a
 * function to register it named `tox_callback_${event}`. Passing a NULL
 * callback will result in no callback being registered for that event. Only
 * one callback per event can be registered, so if a client needs multiple
 * event listeners, it needs to implement the dispatch functionality itself.
 */

/** \subsection threading Threading implications
 *
 * It is possible to run multiple concurrent threads with a Tox instance for
 * each thread. It is also possible to run all Tox instances in the same thread.
 * A common way to run Tox (multiple or single instance) is to have one thread
 * running a simple tox_iteration loop, sleeping for tox_iteration_time
 * milliseconds on each iteration.
 *
 * If you want to access a single Tox instance from multiple threads, access
 * to the instance must be synchronised. While multiple threads can concurrently
 * access multiple different Tox instances, no more than one API function can
 * operate on a single instance at any given time.
 *
 * Functions that write to variable length byte arrays will always have a size
 * function associated with them. The result of this size function is only valid
 * until another mutating function (one that takes a pointer to non-const Tox)
 * is called. Thus, clients must ensure that no other thread calls a mutating
 * function between the call to the size function and the call to the retrieval
 * function.
 *
 * E.g. to get the current nickname, one would write
 *
 * \code
 * size_t length = tox_self_get_name_size(tox);
 * uint8_t *name = malloc(length);
 * if (!name) abort();
 * tox_self_get_name(tox, name);
 * \endcode
 *
 * If any other thread calls tox_self_set_name while this thread is allocating
 * memory, the length will have become invalid, and the call to
 * tox_self_get_name may cause undefined behaviour.
 */

#ifndef TOX_DEFINED
#define TOX_DEFINED
/**
 * The Tox instance type. All the state associated with a connection is held
 * within the instance. Multiple instances can exist and operate concurrently.
 * The maximum number of Tox instances that can exist on a single network
 * device is limited. Note that this is not just a per-process limit, since the
 * limiting factor is the number of usable ports on a device.
 */
typedef struct Tox Tox;
#endif


/*******************************************************************************
 *
 * :: API version
 *
 ******************************************************************************/


/**
 * The major version number. Incremented when the API or ABI changes in an
 * incompatible way.
 */
#define TOX_VERSION_MAJOR		0u
/**
 * The minor version number. Incremented when functionality is added without
 * breaking the API or ABI. Set to 0 when the major version number is
 * incremented.
 */
#define TOX_VERSION_MINOR		0u
/**
 * The patch or revision number. Incremented when bugfixes are applied without
 * changing any functionality or API or ABI.
 */
#define TOX_VERSION_PATCH		0u

/**
 * A macro to check at preprocessing time whether the client code is compatible
 * with the installed version of Tox.
 */
#define TOX_VERSION_IS_API_COMPATIBLE(MAJOR, MINOR, PATCH)	\
  (TOX_VERSION_MAJOR == MAJOR &&				\
   (TOX_VERSION_MINOR > MINOR ||				\
    (TOX_VERSION_MINOR == MINOR &&				\
     TOX_VERSION_PATCH >= PATCH)))

/**
 * A macro to make compilation fail if the client code is not compatible with
 * the installed version of Tox.
 */
#define TOX_VERSION_REQUIRE(MAJOR, MINOR, PATCH)		\
  typedef char tox_required_version[TOX_IS_COMPATIBLE(MAJOR, MINOR, PATCH) ? 1 : -1]


/**
 * Return the major version number of the library. Can be used to display the
 * Tox library version or to check whether the client is compatible with the
 * dynamically linked version of Tox.
 */
uint32_t tox_version_major(void);

/**
 * Return the minor version number of the library.
 */
uint32_t tox_version_minor(void);

/**
 * Return the patch number of the library.
 */
uint32_t tox_version_patch(void);

/**
 * Return whether the compiled library version is compatible with the passed
 * version numbers.
 */
bool tox_version_is_compatible(uint32_t major, uint32_t minor, uint32_t patch);

/**
 * A convenience macro to call tox_version_is_compatible with the currently
 * compiling API version.
 */
#define TOX_VERSION_IS_ABI_COMPATIBLE()				\
  tox_version_is_compatible(TOX_VERSION_MAJOR, TOX_VERSION_MINOR, TOX_VERSION_PATCH)


/*******************************************************************************
 *
 * :: Numeric constants
 *
 ******************************************************************************/


/**
 * The size of a Tox Client ID in bytes.
 */
#define TOX_CLIENT_ID_SIZE		32

/**
 * The size of a Tox address in bytes. Tox addresses are in the format
 * [Client ID (TOX_CLIENT_ID_SIZE bytes)][nospam (4 bytes)][checksum (2 bytes)].
 *
 * The checksum is computed over the Client ID and the nospam value. The first
 * byte is an XOR of all the odd bytes, the second byte is an XOR of all the
 * even bytes of the Client ID and nospam.
 */
#define TOX_ADDRESS_SIZE		(TOX_CLIENT_ID_SIZE + sizeof(uint32_t) + sizeof(uint16_t))

/**
 * Maximum length of a nickname in bytes.
 */
#define TOX_MAX_NAME_LENGTH		128

/**
 * Maximum length of a status message in bytes.
 */
#define TOX_MAX_STATUS_MESSAGE_LENGTH	1007

/**
 * Maximum length of a friend request message in bytes.
 */
#define TOX_MAX_FRIEND_REQUEST_LENGTH	1016

/**
 * Maximum length of a single message after which it should be split.
 */
#define TOX_MAX_MESSAGE_LENGTH		1368

/**
 * Maximum size of custom packets. TODO: should be LENGTH?
 */
#define TOX_MAX_CUSTOM_PACKET_SIZE	1373

/**
 * The number of bytes in a hash generated by tox_hash.
 */
#define TOX_HASH_LENGTH			/*crypto_hash_sha256_BYTES*/ 32

/*******************************************************************************
 *
 * :: Global enumerations
 *
 ******************************************************************************/


/**
 * Represents the possible statuses a client can have.
 */
typedef enum TOX_STATUS {
  /**
   * User is online and available.
   */
  TOX_STATUS_NONE,
  /**
   * User is away. Clients can set this e.g. after a user defined
   * inactivity time.
   */
  TOX_STATUS_AWAY,
  /**
   * User is busy. Signals to other clients that this client does not
   * currently wish to communicate.
   */
  TOX_STATUS_BUSY
} TOX_STATUS;


/*******************************************************************************
 *
 * :: Startup options
 *
 ******************************************************************************/


typedef enum TOX_PROXY_TYPE {
  /**
   * Don't use a proxy.
   */
  TOX_PROXY_TYPE_NONE,
  /**
   * HTTP proxy using CONNECT.
   */
  TOX_PROXY_TYPE_HTTP,
  /**
   * SOCKS proxy for simple socket pipes.
   */
  TOX_PROXY_TYPE_SOCKS5
} TOX_PROXY_TYPE;


/**
 * This struct contains all the startup options for Tox. You can either allocate
 * this object yourself, and pass it to tox_options_default, or call
 * tox_options_new to get a new default options object.
 */
struct Tox_Options {
  /**
   * The type of socket to create.
   *
   * If this is set to false, an IPv4 socket is created, which subsequently
   * only allows IPv4 communication.
   * If it is set to true, an IPv6 socket is created, allowing both IPv4 and
   * IPv6 communication.
   */
  bool ipv6_enabled;

  /**
   * Enable the use of UDP communication when available.
   *
   * Setting this to false will force Tox to use TCP only. Communications will
   * need to be relayed through a TCP relay node, potentially slowing them down.
   * Disabling UDP support is necessary when using anonymous proxies or Tor.
   */
  bool udp_enabled;

  /**
   * Pass communications through a proxy.
   */
  TOX_PROXY_TYPE proxy_type;

  /**
   * The IP address or DNS name of the proxy to be used.
   *
   * If used, this must be non-NULL and be a valid DNS name. The name must not
   * exceed 255 characters, and be in a NUL-terminated C string format
   * (255 chars + 1 NUL byte).
   *
   * This member is ignored (it can be NULL) if proxy_enabled is false.
   */
  char const *proxy_address;

  /**
   * The port to use to connect to the proxy server.
   *
   * Ports must be in the range (1, 65535). The value is ignored if
   * proxy_enabled is false.
   */
  uint16_t proxy_port;
};


/**
 * Initialises a Tox_Options object with the default options.
 *
 * The result of this function is independent of the original options. All
 * values will be overwritten, no values will be read (so it is permissible
 * to pass an uninitialised object).
 *
 * If options is NULL, this function has no effect.
 *
 * @param options An options object to be filled with default options.
 */
void tox_options_default(struct Tox_Options *options);


typedef enum TOX_ERR_OPTIONS_NEW {
  TOX_ERR_OPTIONS_NEW_OK,
  /**
   * The function failed to allocate enough memory for the options struct.
   */
  TOX_ERR_OPTIONS_NEW_MALLOC
} TOX_ERR_OPTIONS_NEW;

/**
 * Allocates a new Tox_Options object and initialises it with the default
 * options. This function can be used to preserve long term ABI compatibility by
 * giving the responsibility of allocation and deallocation to the Tox library.
 *
 * Objects returned from this function must be freed using the tox_options_free
 * function.
 *
 * @return A new Tox_Options object with default options or NULL on failure.
 */
struct Tox_Options *tox_options_new(TOX_ERR_OPTIONS_NEW *error);


/**
 * Releases all resources associated with an options objects.
 *
 * Passing a pointer that was not returned by tox_options_new results in
 * undefined behaviour.
 */
void tox_options_free(struct Tox_Options *options);


/*******************************************************************************
 *
 * :: Creation and destruction
 *
 ******************************************************************************/


typedef enum TOX_ERR_NEW {
  TOX_ERR_NEW_OK,
  TOX_ERR_NEW_NULL,
  /**
   * The function was unable to allocate enough memory to store the internal
   * structures for the Tox object.
   */
  TOX_ERR_NEW_MALLOC,
  /**
   * The function was unable to bind to a port. This may mean that all ports
   * have already been bound, e.g. by other Tox instances, or it may mean
   * a permission error. You may be able to gather more information from errno.
   */
  TOX_ERR_NEW_PORT_ALLOC,
  /**
   * proxy_enabled was true, but the proxy_address passed had an invalid format
   * or was NULL.
   */
  TOX_ERR_NEW_PROXY_BAD_HOST,
  /**
   * proxy_enabled was true, but the proxy_port was invalid.
   */
  TOX_ERR_NEW_PROXY_BAD_PORT,
  /**
   * The proxy address passed could not be resolved.
   */
  TOX_ERR_NEW_PROXY_NOT_FOUND,
  /**
   * The byte array to be loaded contained an encrypted save.
   */
  TOX_ERR_NEW_LOAD_ENCRYPTED,
  /**
   * The data format was invalid. This can happen when loading data that was
   * saved by an older version of Tox, or when the data has been corrupted.
   * When loading from badly formatted data, some data may have been loaded,
   * and the rest is discarded. Passing an invalid length parameter also
   * causes this error.
   */
  TOX_ERR_NEW_LOAD_BAD_FORMAT
} TOX_ERR_NEW;


/**
 * @brief Creates and initialises a new Tox instance with the options passed.
 *
 * This function will bring the instance into a valid state. Running the event
 * loop with a new instance will operate correctly.
 *
 * If the data parameter is not NULL, this function will load the Tox instance
 * from a byte array previously filled by tox_save.
 *
 * If loading failed or succeeded only partially, the new or partially loaded
 * instance is returned and an error code is set.
 *
 * @param options An options object as described above. If this parameter is
 *   NULL, the default options are used.
 * @param data A byte array containing data previously stored by tox_save.
 * @param length The length of the byte array data. If this parameter is 0, the
 *   data parameter is ignored.
 *
 * @see tox_iteration for the event loop.
 */
Tox *tox_new(struct Tox_Options const *options, uint8_t const *data, size_t length, TOX_ERR_NEW *error);


/**
 * Releases all resources associated with the Tox instance and disconnects from
 * the network.
 *
 * After calling this function, the Tox pointer becomes invalid. No other
 * functions can be called, and the pointer value can no longer be read.
 */
void tox_kill(Tox *tox);


/**
 * Calculates the number of bytes required to store the tox instance with
 * tox_save. This function cannot fail. The result is always greater than 0.
 *
 * @see threading for concurrency implications.
 */
size_t tox_save_size(Tox const *tox);

/**
 * Store all information associated with the tox instance to a byte array.
 *
 * @param data A memory region large enough to store the tox instance data.
 *   Call tox_save_size to find the number of bytes required. If this parameter
 *   is NULL, this function has no effect.
 */
void tox_save(Tox const *tox, uint8_t *data);


/*******************************************************************************
 *
 * :: Connection lifecycle and event loop
 *
 ******************************************************************************/


typedef enum TOX_ERR_BOOTSTRAP {
  TOX_ERR_BOOTSTRAP_OK,
  TOX_ERR_BOOTSTRAP_NULL,
  /**
   * The address could not be resolved to an IP address, or the IP address
   * passed was invalid.
   */
  TOX_ERR_BOOTSTRAP_BAD_ADDRESS,
  /**
   * The port passed was invalid. The valid port range is (1, 65535).
   */
  TOX_ERR_BOOTSTRAP_BAD_PORT
} TOX_ERR_BOOTSTRAP;

/**
 * Sends a "get nodes" request to the given bootstrap node with IP, port, and
 * public key to setup connections.
 *
 * This function will attempt to connect to the node using UDP and TCP at the
 * same time.
 *
 * Tox will use the node as a TCP relay in case Tox_Options.udp_enabled was
 * false, and also to connect to friends that are in TCP-only mode. Tox will
 * also use the TCP connection when NAT hole punching is slow, and later switch
 * to UDP if hole punching succeeds.
 *
 * @param address The hostname or IP address (IPv4 or IPv6) of the node.
 * @param port The port on the host on which the bootstrap Tox instance is
 *   listening.
 * @param public_key The long term public key of the bootstrap node
 *   (TOX_CLIENT_ID_SIZE bytes).
 * @return true on success.
 */
bool tox_bootstrap(Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error);


typedef enum TOX_CONNECTION {
  /**
   * There is no connection. This instance, or the friend the state change is
   * about, is now offline.
   */
  TOX_CONNECTION_NONE,
  /**
   * A TCP connection has been established. For the own instance, this means it
   * is connected through a TCP relay, only. For a friend, this means that the
   * connection to that particular friend goes through a TCP relay.
   */
  TOX_CONNECTION_TCP4,
  TOX_CONNECTION_TCP6,
  /**
   * A UDP connection has been established. For the own instance, this means it
   * is able to send UDP packets to DHT nodes, but may still be connected to
   * a TCP relay. For a friend, this means that the connection to that
   * particular friend was built using direct UDP packets.
   */
  TOX_CONNECTION_UDP4,
  TOX_CONNECTION_UDP6
} TOX_CONNECTION;


/**
 * Return whether we are connected to the DHT. The return value is equal to the
 * last value received through the `connection_status` callback.
 */
TOX_CONNECTION tox_get_connection_status(Tox const *tox);

/**
 * The function type for the `connection_status` callback.
 *
 * @param connection_status Equal to the return value of
 *   tox_get_connection_status.
 */
typedef void tox_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data);

/**
 * Set the callback for the `connection_status` event. Pass NULL to unset.
 *
 * This event is triggered whenever there is a change in the DHT connection
 * state. When disconnected, a client may choose to call tox_bootstrap again, to
 * reconnect to the DHT. Note that this state may frequently change for short
 * amounts of time. Clients should therefore not immediately bootstrap on
 * receiving a disconnect.
 *
 * TODO: how long should a client wait before bootstrapping again?
 */
void tox_callback_connection_status(Tox *tox, tox_connection_status_cb *function, void *user_data);


/**
 * Return the time in milliseconds before tox_iteration() should be called again
 * for optimal performance.
 */
uint32_t tox_iteration_interval(Tox const *tox);


/**
 * The main loop that needs to be run in intervals of tox_iteration_interval()
 * milliseconds.
 */
void tox_iteration(Tox *tox);


/*******************************************************************************
 *
 * :: Internal client information (Tox address/id)
 *
 ******************************************************************************/


/**
 * Writes the Tox friend address of the client to a byte array. The address is
 * not in human-readable format. If a client wants to display the address,
 * formatting is required.
 *
 * @param address A memory region of at least TOX_ADDRESS_SIZE bytes. If this
 *   parameter is NULL, this function has no effect.
 * @see TOX_ADDRESS_SIZE for the address format.
 */
void tox_self_get_address(Tox const *tox, uint8_t *address);


/**
 * Set the 4-byte nospam part of the address.
 *
 * @param nospam Any 32 bit unsigned integer.
 */
void tox_self_set_nospam(Tox *tox, uint32_t nospam);

/**
 * Get the 4-byte nospam part of the address.
 */
uint32_t tox_self_get_nospam(Tox const *tox);

/**
 * Copy the Tox Client ID (long term public key) from the Tox object.
 *
 * @param client_id A memory region of at least TOX_CLIENT_ID_SIZE bytes. If
 *   this parameter is NULL, this function has no effect.
 */
void tox_self_get_client_id(Tox const *tox, uint8_t *client_id);

/**
 * Copy the private key from the Tox object.
 *
 * @param private_key A memory region of at least TOX_CLIENT_ID_SIZE bytes. If
 *   this parameter is NULL, this function has no effect.
 */
void tox_self_get_private_key(Tox const *tox, uint8_t *private_key);


/*******************************************************************************
 *
 * :: User-visible client information (nickname/status)
 *
 ******************************************************************************/


/**
 * Common error codes for all functions that set a piece of user-visible
 * client information.
 */
typedef enum TOX_ERR_SET_INFO {
  TOX_ERR_SET_INFO_OK,
  TOX_ERR_SET_INFO_NULL,
  /**
   * Information length exceeded maximum permissible size.
   */
  TOX_ERR_SET_INFO_TOO_LONG
} TOX_ERR_SET_INFO;


/**
 * Set the nickname for the Tox client.
 *
 * Nickname length cannot exceed TOX_MAX_NAME_LENGTH. If length is 0, the name
 * parameter is ignored (it can be NULL), and the nickname is set back to empty.
 *
 * @param name A byte array containing the new nickname.
 * @param length The size of the name byte array.
 *
 * @return true on success.
 */
bool tox_self_set_name(Tox *tox, uint8_t const *name, size_t length, TOX_ERR_SET_INFO *error);

/**
 * Return the length of the current nickname as passed to tox_self_set_name.
 *
 * If no nickname was set before calling this function, the name is empty,
 * and this function returns 0.
 *
 * @see threading for concurrency implications.
 */
size_t tox_self_get_name_size(Tox const *tox);

/**
 * Write the nickname set by tox_self_set_name to a byte array.
 *
 * If no nickname was set before calling this function, the name is empty,
 * and this function has no effect.
 *
 * Call tox_self_get_name_size to find out how much memory to allocate for
 * the result.
 *
 * @param name A valid memory location large enough to hold the nickname.
 *   If this parameter is NULL, the function has no effect.
 */
void tox_self_get_name(Tox const *tox, uint8_t *name);


/**
 * Set the client's status message.
 *
 * Status message length cannot exceed TOX_MAX_STATUS_MESSAGE_LENGTH. If
 * length is 0, the status parameter is ignored (it can be NULL), and the
 * user status is set back to empty.
 */
bool tox_self_set_status_message(Tox *tox, uint8_t const *status, size_t length, TOX_ERR_SET_INFO *error);

/**
 * Return the length of the current status message as passed to
 * tox_self_set_status_message.
 *
 * If no status message was set before calling this function, the status
 * is empty, and this function returns 0.
 *
 * @see threading for concurrency implications.
 */
size_t tox_self_get_status_message_size(Tox const *tox);

/**
 * Write the status message set by tox_self_set_status_message to a byte array.
 *
 * If no status message was set before calling this function, the status is
 * empty, and this function has no effect.
 *
 * Call tox_self_status_message_size to find out how much memory to allocate for
 * the result.
 *
 * @param name A valid memory location large enough to hold the status message.
 *   If this parameter is NULL, the function has no effect.
 */
void tox_self_get_status_message(Tox const *tox, uint8_t *status);


/**
 * Set the client's user status.
 *
 * @param user_status One of the user statuses listed in the enumeration above.
 */
void tox_self_set_status(Tox *tox, TOX_STATUS user_status);

/**
 * Returns the client's user status.
 */
TOX_STATUS tox_self_get_status(Tox const *tox);


/*******************************************************************************
 *
 * :: Friend list management
 *
 ******************************************************************************/


typedef enum TOX_ERR_FRIEND_ADD {
  TOX_ERR_FRIEND_ADD_OK,
  TOX_ERR_FRIEND_ADD_NULL,
  /**
   * The length of the friend request message exceeded
   * TOX_MAX_FRIEND_REQUEST_LENGTH.
   */
  TOX_ERR_FRIEND_ADD_TOO_LONG,
  /**
   * The friend request message was empty. This, and the TOO_LONG code will
   * never be returned from tox_friend_add_norequest.
   */
  TOX_ERR_FRIEND_ADD_NO_MESSAGE,
  /**
   * The friend address belongs to the sending client.
   */
  TOX_ERR_FRIEND_ADD_OWN_KEY,
  /**
   * A friend request has already been sent, or the address belongs to a friend
   * that is already on the friend list.
   */
  TOX_ERR_FRIEND_ADD_ALREADY_SENT,
  /**
   * The friend address checksum failed.
   */
  TOX_ERR_FRIEND_ADD_BAD_CHECKSUM,
  /**
   * The friend was already there, but the nospam value was different.
   */
  TOX_ERR_FRIEND_ADD_SET_NEW_NOSPAM,
  /**
   * A memory allocation failed when trying to increase the friend list size.
   */
  TOX_ERR_FRIEND_ADD_MALLOC
} TOX_ERR_FRIEND_ADD;

/**
 * Add a friend to the friend list and send a friend request.
 *
 * A friend request message must be at least 1 byte long and at most
 * TOX_MAX_FRIEND_REQUEST_LENGTH.
 *
 * Friend numbers are unique identifiers used in all functions that operate on
 * friends. Once added, a friend number is stable for the lifetime of the Tox
 * object. After saving the state and reloading it, the friend numbers may not
 * be the same as before. Deleting a friend creates a gap in the friend number
 * set, which is filled by the next adding of a friend.
 *
 * If more than UINT32_MAX friends are added, this function causes undefined
 * behaviour.
 *
 * @param address The address of the friend (returned by tox_self_get_address of
 *   the friend you wish to add) it must be TOX_ADDRESS_SIZE bytes.
 * @param message The message that will be sent along with the friend request.
 * @param length The length of the data byte array.
 *
 * @return the friend number.
 */
uint32_t tox_friend_add(Tox *tox, uint8_t const *address, uint8_t const *message, size_t length, TOX_ERR_FRIEND_ADD *error);


/**
 * Add a friend without sending a friend request.
 *
 * This function is used to add a friend in response to a friend request. If the
 * client receives a friend request, it can be reasonably sure that the other
 * client added this client as a friend, eliminating the need for a friend
 * request.
 *
 * This function is also useful in a situation where both instances are
 * controlled by the same entity, so that this entity can perform the mutual
 * friend adding. In this case, there is no need for a friend request, either.
 *
 * @param client_id A byte array of length TOX_CLIENT_ID_SIZE containing the
 *   Client ID (not the Address) of the friend to add.
 * @see tox_friend_add for a more detailed description of friend numbers.
 */
uint32_t tox_friend_add_norequest(Tox *tox, uint8_t const *client_id, TOX_ERR_FRIEND_ADD *error);


typedef enum TOX_ERR_FRIEND_DELETE {
  TOX_ERR_FRIEND_DELETE_OK,
  /**
   * There was no friend with the given friend number. No friends were deleted.
   */
  TOX_ERR_FRIEND_DELETE_FRIEND_NOT_FOUND
} TOX_ERR_FRIEND_DELETE;

/**
 * Remove a friend from the friend list.
 *
 * This does not notify the friend of their deletion. After calling this
 * function, this client will appear offline to the friend and no communication
 * can occur between the two.
 *
 * @friend_number Friend number for the friend to be deleted.
 *
 * @return true on success.
 */
bool tox_friend_delete(Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_DELETE *error);


/*******************************************************************************
 *
 * :: Friend list queries
 *
 ******************************************************************************/


typedef enum TOX_ERR_FRIEND_BY_CLIENT_ID {
  TOX_ERR_FRIEND_BY_CLIENT_ID_OK,
  TOX_ERR_FRIEND_BY_CLIENT_ID_NULL,
  /**
   * No friend with the given Client ID exists on the friend list.
   */
  TOX_ERR_FRIEND_BY_CLIENT_ID_NOT_FOUND
} TOX_ERR_FRIEND_BY_CLIENT_ID;

/**
 * Return the friend number associated with that Client ID.
 *
 * @param client_id A byte array containing the Client ID.
 */
uint32_t tox_friend_by_client_id(Tox const *tox, uint8_t const *client_id, TOX_ERR_FRIEND_BY_CLIENT_ID *error);


typedef enum TOX_ERR_GET_CLIENT_ID {
  TOX_ERR_FRIEND_GET_CLIENT_ID_OK,
  /**
   * No friend with the given number exists on the friend list.
   */
  TOX_ERR_FRIEND_GET_CLIENT_ID_FRIEND_NOT_FOUND
} TOX_ERR_FRIEND_GET_CLIENT_ID;

/**
 * Copies the Client ID associated with a given friend number to a byte array.
 *
 * @param friend_number The friend number you want the Client ID of.
 * @param client_id A memory region of at least TOX_CLIENT_ID_SIZE bytes. If
 *   this parameter is NULL, this function has no effect.
 *
 * @return true on success.
 */
bool tox_friend_get_client_id(Tox const *tox, uint32_t friend_number, uint8_t *client_id, TOX_ERR_GET_CLIENT_ID *error);


/**
 * Checks if a friend with the given friend number exists and returns true if
 * it does.
 */
bool tox_friend_exists(Tox const *tox, uint32_t friend_number);


/**
 * Return the number of friends on the friend list.
 *
 * This function can be used to determine how much memory to allocate for
 * tox_friend_list.
 */
size_t tox_friend_list_size(Tox const *tox);


/**
 * Copy a list of valid friend numbers into an array.
 *
 * Call tox_friend_list_size to determine the number of elements to allocate.
 *
 * @param list A memory region with enough space to hold the friend list. If
 *   this parameter is NULL, this function has no effect.
 */
void tox_friend_list(Tox const *tox, uint32_t *list);



/*******************************************************************************
 *
 * :: Friend-specific state queries (can also be received through callbacks)
 *
 ******************************************************************************/


/**
 * Common error codes for friend state query functions.
 */
typedef enum TOX_ERR_FRIEND_QUERY {
  TOX_ERR_FRIEND_QUERY_OK,
  /**
   * The pointer parameter for storing the query result (name, message) was
   * NULL. Unlike the `_self_` variants of these functions, which have no effect
   * when a parameter is NULL, these functions return an error in that case.
   */
  TOX_ERR_FRIEND_QUERY_NULL,
  /**
   * The friend_number did not designate a valid friend.
   */
  TOX_ERR_FRIEND_QUERY_FRIEND_NOT_FOUND
} TOX_ERR_FRIEND_QUERY;


/**
 * Return the length of the friend's name. If the friend number is invalid, the
 * return value is unspecified.
 *
 * The return value is equal to the `length` argument received by the last
 * `friend_name` callback.
 */
size_t tox_friend_get_name_size(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

/**
 * Write the name of the friend designated by the given friend number to a byte
 * array.
 *
 * Call tox_friend_get_name_size to determine the allocation size for the `name`
 * parameter.
 *
 * The data written to `name` is equal to the data received by the last
 * `friend_name` callback.
 *
 * @param name A valid memory region large enough to store the friend's name.
 *
 * @return true on success.
 */
bool tox_friend_get_name(Tox const *tox, uint32_t friend_number, uint8_t *name, TOX_ERR_FRIEND_QUERY *error);

/**
 * The function type for the `friend_name` callback.
 *
 * @param friend_number The friend number of the friend whose name changed.
 * @param name A byte array containing the same data as
 *   tox_friend_get_name would write to its `name` parameter.
 * @param length A value equal to the return value of
 *   tox_friend_get_name_size.
 */
typedef void tox_friend_name_cb(Tox *tox, uint32_t friend_number, uint8_t const *name, size_t length, void *user_data);

/**
 * Set the callback for the `friend_name` event. Pass NULL to unset.
 *
 * This event is triggered when a friend changes their name.
 */
void tox_callback_friend_name(Tox *tox, tox_friend_name_cb *function, void *user_data);


/**
 * Return the length of the friend's status message. If the friend number is
 * invalid, the return value is unspecified.
 */
size_t tox_friend_get_status_message_size(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

/**
 * Write the name of the friend designated by the given friend number to a byte
 * array.
 *
 * Call tox_friend_get_name_size to determine the allocation size for the `name`
 * parameter.
 *
 * @param name A valid memory region large enough to store the friend's name.
 */
bool tox_friend_get_status_message(Tox const *tox, uint32_t friend_number, uint8_t *message, TOX_ERR_FRIEND_QUERY *error);

/**
 * The function type for the `friend_status_message` callback.
 *
 * @param friend_number The friend number of the friend whose status message
 *   changed.
 * @param message A byte array containing the same data as
 *   tox_friend_get_status_message would write to its `message` parameter.
 * @param length A value equal to the return value of
 *   tox_friend_get_status_message_size.
 */
typedef void tox_friend_status_message_cb(Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, void *user_data);

/**
 * Set the callback for the `friend_status_message` event. Pass NULL to unset.
 *
 * This event is triggered when a friend changes their name.
 */
void tox_callback_friend_status_message(Tox *tox, tox_friend_status_message_cb *function, void *user_data);


/**
 * Return the friend's user status (away/busy/...). If the friend number is
 * invalid, the return value is unspecified.
 */
TOX_STATUS tox_friend_get_status(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

/**
 * The function type for the `friend_status` callback.
 *
 * @param friend_number The friend number of the friend whose user status
 *   changed.
 * @param status The new user status.
 */
typedef void tox_friend_status_cb(Tox *tox, uint32_t friend_number, TOX_STATUS status, void *user_data);

/**
 * Set the callback for the `friend_status` event. Pass NULL to unset.
 *
 * This event is triggered when a friend changes their user status.
 */
void tox_callback_friend_status(Tox *tox, tox_friend_status_cb *function, void *user_data);


/**
 * Check whether a friend is currently connected to this client.
 *
 * The result of this function is equal to the last value received by the
 * `friend_connection_status` callback.
 *
 * @param friend_number The friend number for which to query the connection
 *   status.
 *
 * @return the friend's connection status as it was received through the
 *   `friend_connection_status` event.
 */
TOX_CONNECTION tox_friend_get_connection_status(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

/**
 * The function type for the `friend_connection_status` callback.
 *
 * @param friend_number The friend number of the friend whose connection status
 *   changed.
 * @param connection_status The result of calling
 *   tox_friend_get_connection_status on the passed friend_number.
 */
typedef void tox_friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data);

/**
 * Set the callback for the `friend_connection_status` event. Pass NULL to
 * unset.
 *
 * This event is triggered when a friend goes offline after having been online,
 * or when a friend goes online.
 *
 * This callback is not called when adding friends. It is assumed that when
 * adding friends, their connection status is offline.
 */
void tox_callback_friend_connection_status(Tox *tox, tox_friend_connection_status_cb *function, void *user_data);


/**
 * Check whether a friend is currently typing a message.
 *
 * @param friend_number The friend number for which to query the typing status.
 *
 * @return true if the friend is typing.
 * @return false if the friend is not typing, or the friend number was
 *   invalid. Inspect the error code to determine which case it is.
 */
bool tox_friend_get_typing(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error);

/**
 * The function type for the `friend_typing` callback.
 *
 * @param friend_number The friend number of the friend who started or stopped
 *   typing.
 * @param is_typing The result of calling tox_friend_get_typing on the passed
 *   friend_number.
 */
typedef void tox_friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data);

/**
 * Set the callback for the `friend_typing` event. Pass NULL to unset.
 *
 * This event is triggered when a friend starts or stops typing.
 */
void tox_callback_friend_typing(Tox *tox, tox_friend_typing_cb *function, void *user_data);


/*******************************************************************************
 *
 * :: Sending private messages
 *
 ******************************************************************************/


typedef enum TOX_ERR_SET_TYPING {
  TOX_ERR_SET_TYPING_OK,
  /**
   * The friend number did not designate a valid friend.
   */
  TOX_ERR_SET_TYPING_FRIEND_NOT_FOUND
} TOX_ERR_SET_TYPING;

/**
 * Set the client's typing status for a friend.
 *
 * The client is responsible for turning it on or off.
 *
 * @param friend_number The friend to which the client is typing a message.
 * @param is_typing The typing status. True means the client is typing.
 *
 * @return true on success.
 */
bool tox_self_set_typing(Tox *tox, uint32_t friend_number, bool is_typing, TOX_ERR_SET_TYPING *error);


typedef enum TOX_ERR_SEND_MESSAGE {
  TOX_ERR_SEND_MESSAGE_OK,
  TOX_ERR_SEND_MESSAGE_NULL,
  /**
   * The friend number did not designate a valid friend.
   */
  TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND,
  /**
   * This client is currently not connected to the friend.
   */
  TOX_ERR_SEND_MESSAGE_FRIEND_NOT_CONNECTED,
  /**
   * An allocation error occurred while increasing the send queue size.
   */
  TOX_ERR_SEND_MESSAGE_SENDQ,
  /**
   * Message length exceeded TOX_MAX_MESSAGE_LENGTH.
   */
  TOX_ERR_SEND_MESSAGE_TOO_LONG,
  /**
   * Attempted to send a zero-length message.
   */
  TOX_ERR_SEND_MESSAGE_EMPTY
} TOX_ERR_SEND_MESSAGE;

/**
 * Send a text chat message to an online friend.
 *
 * This function creates a chat message packet and pushes it into the send
 * queue.
 *
 * The message length may not exceed TOX_MAX_MESSAGE_LENGTH. Larger messages
 * must be split by the client and sent as separate messages. Other clients can
 * then reassemble the fragments. Messages may not be empty.
 *
 * The return value of this function is the message ID. If a read receipt is
 * received, the triggered `read_receipt` event will be passed this message ID.
 *
 * Message IDs are unique per friend. The first message ID is 0. Message IDs are
 * incremented by 1 each time a message is sent. If UINT32_MAX messages were
 * sent, the next message ID is 0.
 */
uint32_t tox_send_message(Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, TOX_ERR_SEND_MESSAGE *error);


/**
 * Send an action to an online friend.
 *
 * This is similar to /me (CTCP ACTION) on IRC.
 *
 * Message ID space is shared between tox_send_message and tox_send_action. This
 * means that sending a message will cause the next message ID from sending an
 * action will be incremented.
 *
 * @see tox_send_message for more details.
 */
uint32_t tox_send_action(Tox *tox, uint32_t friend_number, uint8_t const *action, size_t length, TOX_ERR_SEND_MESSAGE *error);


/**
 * The function type for the `read_receipt` callback.
 *
 * @param friend_number The friend number of the friend who received the message.
 * @param message_id The message ID as returned from tox_send_message or
 *   tox_send_action corresponding to the message sent.
 */
typedef void tox_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data);

/**
 * Set the callback for the `read_receipt` event. Pass NULL to unset.
 *
 * This event is triggered when a read receipt is received from a friend. This
 * normally means that the message has been received by the friend, however a
 * friend can send a read receipt with any message ID in it, so the number
 * received here may not correspond to any message sent through tox_send_message
 * or tox_send_action. In that case, the receipt should be discarded.
 */
void tox_callback_read_receipt(Tox *tox, tox_read_receipt_cb *function, void *user_data);


/*******************************************************************************
 *
 * :: Receiving private messages and friend requests
 *
 ******************************************************************************/


/**
 * The function type for the `friend_request` callback.
 *
 * @param client_id The Client ID of the user who sent the friend request.
 * @param time_delta A delta in seconds between when the message was composed
 *   and when it is being transmitted. For messages that are sent immediately,
 *   it will be 0. If a message was written and couldn't be sent immediately
 *   (due to a connection failure, for example), the time_delta is an
 *   approximation of when it was composed.
 * @param message The message they sent along with the request.
 * @param length The size of the message byte array.
 */
typedef void tox_friend_request_cb(Tox *tox, uint8_t const *client_id, /*uint32_t time_delta, */uint8_t const *message, size_t length, void *user_data);

/**
 * Set the callback for the `friend_request` event. Pass NULL to unset.
 *
 * This event is triggered when a friend request is received.
 */
void tox_callback_friend_request(Tox *tox, tox_friend_request_cb *function, void *user_data);


/**
 * The function type for the `friend_message` callback.
 *
 * @param friend_number The friend number of the friend who sent the message.
 * @param time_delta Time between composition and sending.
 * @param message The message data they sent.
 * @param length The size of the message byte array.
 *
 * @see tox_friend_request_cb for more information on time_delta.
 */
typedef void tox_friend_message_cb(Tox *tox, uint32_t friend_number, /*uint32_t time_delta, */uint8_t const *message, size_t length, void *user_data);

/**
 * Set the callback for the `friend_message` event. Pass NULL to unset.
 *
 * This event is triggered when a message from a friend is received.
 */
void tox_callback_friend_message(Tox *tox, tox_friend_message_cb *function, void *user_data);


/**
 * The function type for the `friend_action` callback.
 *
 * @param friend_number The friend number of the friend who sent the action.
 * @param time_delta Time between composition and sending.
 * @param action The action message data they sent.
 * @param length The size of the action byte array.
 *
 * @see tox_friend_request_cb for more information on time_delta.
 */
typedef void tox_friend_action_cb(Tox *tox, uint32_t friend_number, /*uint32_t time_delta, */uint8_t const *action, size_t length, void *user_data);

/**
 * Set the callback for the `friend_action` event. Pass NULL to unset.
 *
 * This event is triggered when an action from a friend is received.
 */
void tox_callback_friend_action(Tox *tox, tox_friend_action_cb *function, void *user_data);



/*******************************************************************************
 *
 * :: File transmission: common between sending and receiving
 *
 ******************************************************************************/


typedef enum TOX_FILE_KIND {
  /**
   * Arbitrary file data. Clients can choose to handle it based on the file name
   * or magic or any other way they choose.
   */
  TOX_FILE_KIND_DATA,
  /**
   * Avatar data. This consists of tox_hash(image) + image.
   *
   * Avatars can be sent at any time the client wishes. Generally, a client will
   * send the avatar to a friend when that friend comes online, and to all
   * friends when the avatar changed. A client can save some traffic by
   * remembering which friend received the updated avatar already and only send
   * it if the friend has an out of date avatar.
   *
   * Clients who receive avatar send requests can reject it (by sending
   * TOX_FILE_CONTROL_CANCEL before any other controls), or accept it (by
   * sending TOX_FILE_CONTROL_RESUME). The first chunk will contain the hash in
   * its first TOX_HASH_LENGTH bytes. A client can compare this hash with a
   * saved hash and send TOX_FILE_CONTROL_CANCEL to terminate the avatar
   * transfer if it matches.
   */
  TOX_FILE_KIND_AVATAR
} TOX_FILE_KIND;


/**
 * Generates a cryptographic hash of the given data.
 *
 * This function may be used by clients for any purpose, but is provided
 * primarily for validating cached avatars. This use is highly recommended to
 * avoid unnecessary avatar updates.
 *
 * If length is zero or data is NULL, the hash will contain all zero. If hash is
 * NULL, the function returns false, otherwise it returns true.
 *
 * This function is a wrapper to internal message-digest functions.
 *
 * @param hash A valid memory location the hash data. It must be at least
 *   TOX_HASH_LENGTH bytes in size.
 * @param data Data to be hashed or NULL.
 * @param length Size of the data array or 0.
 *
 * @return true if hash was not NULL.
 */
bool tox_hash(uint8_t *hash, uint8_t const *data, size_t length);


typedef enum TOX_FILE_CONTROL {
  /**
   * Sent by the receiving side to accept a file send request. Also sent after a
   * TOX_FILE_CONTROL_PAUSE command to continue sending or receiving.
   */
  TOX_FILE_CONTROL_RESUME,
  /**
   * Sent by clients to pause the file transfer. The initial state of a file
   * transfer is always paused on the receiving side and running on the sending
   * side. If both the sending and receiving side pause the transfer, then both
   * need to send TOX_FILE_CONTROL_RESUME for the transfer to resume.
   */
  TOX_FILE_CONTROL_PAUSE,
  /**
   * Sent by the receiving side to reject a file send request before any other
   * commands are sent. Also sent by either side to terminate a file transfer.
   */
  TOX_FILE_CONTROL_CANCEL
} TOX_FILE_CONTROL;


typedef enum TOX_ERR_FILE_CONTROL {
  TOX_ERR_FILE_CONTROL_OK,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOX_ERR_FILE_CONTROL_FRIEND_NOT_FOUND,
  /**
   * This client is currently not connected to the friend.
   */
  TOX_ERR_FILE_CONTROL_FRIEND_NOT_CONNECTED,
  /**
   * No file transfer with the given file number was found for the given friend.
   */
  TOX_ERR_FILE_CONTROL_NOT_FOUND,
  /**
   * A RESUME control was sent, but the file transfer is running normally.
   */
  TOX_ERR_FILE_CONTROL_NOT_PAUSED,
  /**
   * A RESUME control was sent, but the file transfer was paused by the other
   * party. Only the party that paused the transfer can resume it.
   */
  TOX_ERR_FILE_CONTROL_DENIED,
  /**
   * A PAUSE control was sent, but the file transfer was already paused.
   */
  TOX_ERR_FILE_CONTROL_ALREADY_PAUSED
} TOX_ERR_FILE_CONTROL;

/**
 * Sends a file control command to a friend for a given file transfer.
 *
 * @param friend_number The friend number of the friend the file is being
 *   transferred to.
 * @param file_number The friend-specific identifier for the file transfer.
 * @param control The control command to send.
 *
 * @return true on success.
 */
bool tox_file_control(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, TOX_ERR_FILE_CONTROL *error);


/**
 * The function type for the `file_control` callback.
 *
 * When receiving TOX_FILE_CONTROL_CANCEL, the client should release the
 * resources associated with the file number and consider the transfer failed.
 *
 * @param friend_number The friend number of the friend who is sending the file.
 * @param file_number The friend-specific file number the data received is
 *   associated with.
 * @param control The file control command received.
 */
typedef void tox_file_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, void *user_data);

/**
 * Set the callback for the `file_control` event. Pass NULL to unset.
 *
 * This event is triggered when a file control command is received from a
 * friend.
 */
void tox_callback_file_control(Tox *tox, tox_file_control_cb *function, void *user_data);


/*******************************************************************************
 *
 * :: File transmission: sending
 *
 ******************************************************************************/


typedef enum TOX_ERR_FILE_SEND {
  TOX_ERR_FILE_SEND_OK,
  TOX_ERR_FILE_SEND_NULL,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND,
  /**
   * This client is currently not connected to the friend.
   */
  TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED,
  /**
   * Filename length was 0.
   */
  TOX_ERR_FILE_SEND_NAME_EMPTY,
  /**
   * Filename length exceeded 255 bytes.
   */
  TOX_ERR_FILE_SEND_NAME_TOO_LONG,
  /**
   * Too many ongoing transfers. The maximum number of concurrent file transfers
   * is 256 per friend per direction (sending and receiving).
   */
  TOX_ERR_FILE_SEND_TOO_MANY
} TOX_ERR_FILE_SEND;

/**
 * Send a file transmission request.
 *
 * Maximum filename length is 255 bytes. The filename should generally just be
 * a file name, not a path with directory names.
 *
 * If a non-zero file size is provided, this can be used by both sides to
 * determine the sending progress. File size can be set to zero for streaming
 * data of unknown size.
 *
 * File transmission occurs in chunks, which are requested through the
 * `file_request_chunk` event.
 *
 * File numbers are stable across tox_save/tox_load cycles, so that file
 * transfers can be resumed when a client restarts. The client needs to
 * associate (friend Client ID, file number) with the local path of the file and
 * persist this information to support resuming of transfers across restarts.
 *
 * If the file contents change during a transfer, the behaviour is unspecified
 * in general. What will actually happen depends on the mode in which the file
 * was modified and how the client determines the file size.
 *
 * - If the file size was increased
 *   - and sending mode was streaming (file_size = 0), the behaviour will be as
 *     expected.
 *   - and sending mode was file (file_size != 0), the file_request_chunk
 *     callback will receive length = 0 when Core thinks the file transfer has
 *     finished. If the client remembers the file size as it was when sending
 *     the request, it will terminate the transfer normally. If the client
 *     re-reads the size, it will think the friend cancelled the transfer.
 * - If the file size was decreased
 *   - and sending mode was streaming, the behaviour is as expected.
 *   - and sending mode was file, the callback will return 0 at the new
 *     (earlier) end-of-file, signalling to the friend that the transfer was
 *     cancelled.
 * - If the file contents were modified
 *   - at a position before the current read, the two files (local and remote)
 *     will differ after the transfer terminates.
 *   - at a position after the current read, the file transfer will succeed as
 *     expected.
 *   - In either case, both sides will regard the transfer as complete and
 *     successful.
 *
 * @param friend_number The friend number of the friend the file send request
 *   should be sent to.
 * @param kind The meaning of the file to be sent.
 * @param file_size Size in bytes of the file the client wants to send, 0 if
 *   unknown or streaming.
 * @param filename Name of the file. Does not need to be the actual name. This
 *   name will be sent along with the file send request.
 * @param filename_length Size in bytes of the filename.
 *
 * @return A file number used as an identifier in subsequent callbacks. This
 *   number is per friend. File numbers are reused after a transfer terminates.
 */
uint32_t tox_file_send(Tox *tox, uint32_t friend_number, TOX_FILE_KIND kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, TOX_ERR_FILE_SEND *error);


typedef enum TOX_ERR_FILE_SEND_CHUNK {
  TOX_ERR_FILE_SEND_CHUNK_OK,
  /**
   * The length parameter was non-zero, but data was NULL.
   */
  TOX_ERR_FILE_SEND_CHUNK_NULL,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND,
  /**
   * This client is currently not connected to the friend.
   */
  TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED,
  /**
   * No file transfer with the given file number was found for the given friend.
   */
  TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND,
  /**
   * Attempted to send more data than requested. The requested data size is
   * adjusted according to maximum transmission unit and the expected end of
   * the file. Trying to send more will result in no data being sent.
   */
  TOX_ERR_FILE_SEND_CHUNK_TOO_LARGE
} TOX_ERR_FILE_SEND_CHUNK;

/**
 * Send a chunk of file data to a friend.
 *
 * This function is called in response to the `file_request_chunk` callback. The
 * length parameter should be equal to or less than the one received though the
 * callback. If it is zero, the transfer is assumed complete. For files with
 * known size, Core will know that the transfer is complete after the last byte
 * has been received, so it is not necessary (though not harmful) to send a
 * zero-length chunk to terminate. For streams, it is necessary for the last
 * chunk sent to be zero-length.
 *
 * @return true on success.
 */
bool tox_file_send_chunk(Tox *tox, uint32_t friend_number, uint32_t file_number, uint8_t const *data, size_t length, TOX_ERR_FILE_SEND_CHUNK *error);


/**
 * The function type for the `file_request_chunk` callback.
 *
 * If the length parameter is 0, the file transfer is finished, and the client's
 * resources associated with the file number should be released. After a call
 * with zero length, the file number can be reused for future file transfers.
 *
 * If the requested position is not equal to the client's idea of the current
 * file or stream position, it will need to seek. In case of read-once streams,
 * the client should keep the last read chunk so that a seek back can be
 * supported. A seek-back only ever needs to read from the last requested chunk.
 * This happens when a chunk was requested, but the send failed. A seek-back
 * request can occur an arbitrary number of times for any given chunk.
 *
 * In response to receiving this callback, the client should call the function
 * `tox_file_send_chunk` with the requested chunk. If the number of bytes sent
 * through that function is zero, the file transfer is assumed complete. A
 * client may choose to send less than requested, if it is reading from a
 * stream that doesn't have more data, yet, and it still wants to send some
 * data to the other side. However, this will generally be less efficient than
 * waiting for a full chunk size of data to be ready.
 *
 * @param friend_number The friend number of the receiving friend for this file.
 * @param file_number The file transfer identifier returned by tox_file_send.
 * @param position The file or stream position from which to continue reading.
 * @param length The number of bytes requested for the current chunk.
 */
typedef void tox_file_request_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length, void *user_data);

/**
 * Set the callback for the `file_request_chunk` event. Pass NULL to unset.
 */
void tox_callback_file_request_chunk(Tox *tox, tox_file_request_chunk_cb *function, void *user_data);


/*******************************************************************************
 *
 * :: File transmission: receiving
 *
 ******************************************************************************/


/**
 * The function type for the `file_receive` callback.
 *
 * The client should acquire resources to be associated with the file transfer.
 * Incoming file transfers start in the PAUSED state. After this callback
 * returns, a transfer can be rejected by sending a TOX_FILE_CONTROL_CANCEL
 * control command before any other control commands. It can be accepted by
 * sending TOX_FILE_CONTROL_RESUME.
 *
 * @param friend_number The friend number of the friend who is sending the file
 *   transfer request.
 * @param file_number The friend-specific file number the data received is
 *   associated with.
 */
typedef void tox_file_receive_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_KIND kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, void *user_data);

/**
 * Set the callback for the `file_receive` event. Pass NULL to unset.
 *
 * This event is triggered when a file transfer request is received.
 */
void tox_callback_file_receive(Tox *tox, tox_file_receive_cb *function, void *user_data);


/**
 * The function type for the `file_receive_chunk` callback.
 *
 * This function is first called when a file transfer request is received, and
 * subsequently when a chunk of file data for an accepted request was received.
 *
 * When length is 0, the transfer is finished and the client should release the
 * resources it acquired for the transfer. After a call with length = 0, the
 * file number can be reused for new file transfers.
 *
 * If position is equal to file_size (received in the file_receive callback)
 * when the transfer finishes, the file was received completely. Otherwise, if
 * file_size was 0, streaming ended successfully when length is 0.
 *
 * @param friend_number The friend number of the friend who is sending the file.
 * @param file_number The friend-specific file number the data received is
 *   associated with.
 * @param position The file position of the first byte in data.
 * @param data A byte array containing the received chunk.
 * @param length The length of the received chunk.
 */
typedef void tox_file_receive_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, uint8_t const *data, size_t length, void *user_data);

/**
 * Set the callback for the `file_receive_chunk` event. Pass NULL to unset.
 */
void tox_callback_file_receive_chunk(Tox *tox, tox_file_receive_chunk_cb *function, void *user_data);


/*******************************************************************************
 *
 * :: Group chat management
 *
 ******************************************************************************/


/******************************************************************************
 *
 * :: Group chat message sending and receiving
 *
 ******************************************************************************/


/*******************************************************************************
 *
 * :: Low-level custom packet sending and receiving
 *
 ******************************************************************************/


typedef enum TOX_ERR_SEND_CUSTOM_PACKET {
  TOX_ERR_SEND_CUSTOM_PACKET_OK,
  TOX_ERR_SEND_CUSTOM_PACKET_NULL,
  /**
   * The friend number did not designate a valid friend.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_FOUND,
  /**
   * This client is currently not connected to the friend.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_CONNECTED,
  /**
   * The first byte of data was not in the specified range for the packet type.
   * This range is 200-254 for lossy, and 160-191 for lossless packets.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_INVALID,
  /**
   * Attempted to send an empty packet.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_EMPTY,
  /**
   * Packet data length exceeded TOX_MAX_CUSTOM_PACKET_SIZE.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_TOO_LONG,
  /**
   * Send queue size exceeded.
   */
  TOX_ERR_SEND_CUSTOM_PACKET_SENDQ
} TOX_ERR_SEND_CUSTOM_PACKET;


/**
 * Send a custom lossy packet to a friend.
 *
 * The first byte of data must be in the range 200-254. Maximum length of a
 * custom packet is TOX_MAX_CUSTOM_PACKET_SIZE.
 *
 * Lossy packets behave like UDP packets, meaning they might never reach the
 * other side or might arrive more than once (if someone is messing with the
 * connection) or might arrive in the wrong order.
 *
 * Unless latency is an issue, it is recommended that you use lossless custom
 * packets instead.
 *
 * @param friend_number The friend number of the friend this lossy packet
 *   should be sent to.
 * @param data A byte array containing the packet data.
 * @param length The length of the packet data byte array.
 *
 * @return true on success.
 */
bool tox_send_lossy_packet(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error);

/**
 * The function type for the `friend_lossy_packet` callback.
 *
 * @param friend_number The friend number of the friend who sent a lossy packet.
 * @param data A byte array containing the received packet data.
 * @param length The length of the packet data byte array.
 */
typedef void tox_friend_lossy_packet_cb(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, void *user_data);

/**
 * Set the callback for the `friend_lossy_packet` event. Pass NULL to unset.
 */
void tox_callback_friend_lossy_packet(Tox *tox, tox_friend_lossy_packet_cb *function, void *user_data);


/**
 * Send a custom lossless packet to a friend.
 *
 * The first byte of data must be in the range 160-191. Maximum length of a
 * custom packet is TOX_MAX_CUSTOM_PACKET_SIZE.
 *
 * Lossless packet behaviour is comparable to TCP (reliability, arrive in order)
 * but with packets instead of a stream.
 *
 * @param friend_number The friend number of the friend this lossless packet
 *   should be sent to.
 * @param data A byte array containing the packet data.
 * @param length The length of the packet data byte array.
 *
 * @return true on success.
 */
bool tox_send_lossless_packet(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error);

/**
 * The function type for the `friend_lossless_packet` callback.
 *
 * @param friend_number The friend number of the friend who sent the packet.
 * @param data A byte array containing the received packet data.
 * @param length The length of the packet data byte array.
 */
typedef void tox_friend_lossless_packet_cb(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, void *user_data);

/**
 * Set the callback for the `friend_lossless_packet` event. Pass NULL to unset.
 */
void tox_callback_friend_lossless_packet(Tox *tox, tox_friend_lossless_packet_cb *function, void *user_data);



/*******************************************************************************
 *
 * :: Low-level network information
 *
 ******************************************************************************/


/**
 * Writes the temporary DHT public key of this instance to a byte array.
 *
 * This can be used in combination with an externally accessible IP address and
 * the bound port (from tox_get_udp_port) to run a temporary bootstrap node.
 *
 * Be aware that every time a new instance is created, the DHT public key
 * changes, meaning this cannot be used to run a permanent bootstrap node.
 *
 * @param dht_id A memory region of at least TOX_CLIENT_ID_SIZE bytes. If this
 *   parameter is NULL, this function has no effect.
 */
void tox_get_dht_id(Tox const *tox, uint8_t *dht_id);


typedef enum TOX_ERR_GET_PORT {
  TOX_ERR_GET_PORT_OK,
  /**
   * The instance was not bound to any port.
   */
  TOX_ERR_GET_PORT_NOT_BOUND
} TOX_ERR_GET_PORT;

/**
 * Return the UDP port this Tox instance is bound to.
 */
uint16_t tox_get_udp_port(Tox const *tox, TOX_ERR_GET_PORT *error);

/**
 * Return the TCP port this Tox instance is bound to. This is only relevant if
 * the instance is acting as a TCP relay.
 */
uint16_t tox_get_tcp_port(Tox const *tox, TOX_ERR_GET_PORT *error);


#ifdef __cplusplus
}
#endif
