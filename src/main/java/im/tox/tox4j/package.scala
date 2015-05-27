package im.tox

/**
 * Low level toxcore network interfaces. See [[client]] for the high level API.
 *
 * The packages are organised by subsystem:
 * - A/V in [[tox4j.av]]
 * - Regular text messaging and group chats in [[tox4j.core]]
 * - Cryptographic primitives in [[tox4j.crypto]]
 *
 * The subsystem packages define data structures and interfaces for all low level toxcore operations. Various
 * implementations of these interfaces is contained within subpackages of the [[tox4j.impl]] package. The native
 * interface to the C library libtoxcore is located in [[tox4j.impl.jni]].
 *
 * Each subsystem package contains one or more of the following subpackages:
 * - callbacks: Interfaces for the callbacks (event listeners). This package provides a combined Listener interface
 *     (e.g. [[tox4j.core.callbacks.ToxEventListener]] which inherits from all the single callback interfaces
 *     in the package. An Adapter class (e.g. [[tox4j.core.callbacks.ToxEventAdapter]]) provides empty default
 *     implementations for all callbacks.
 *     Each callback interface is named SomeThingCallback and contains a someThing method and an empty
 *     implementation of itself named IGNORE (e.g. [[tox4j.core.callbacks.ConnectionStatusCallback.IGNORE]].
 * - enums: Java enums mirroring the C versions exactly. The enums' ordinal does not necessarily correspond to the C
 *     enum value, but the JNI bridge needs to know the ordinals, so if you reorder these, then you must update the
 *     bridge, as well.
 * - exceptions: Exception classes corresponding to the C API's error codes. One exception class for each error enum in
 *     the C API. Each exception class contains a Code enum (e.g. [[tox4j.core.exceptions.ToxBootstrapException.Code]])
 *     with all the errors that can occur in Java.
 *
 * Every package 'thing' that has a callbacks subpackage also contains an AbstractToxThing (e.g.
 * [[tox4j.core.AbstractToxCore]]) that implements the "register all callbacks" method in terms of the separate callback
 * methods.
 */
package object tox4j
