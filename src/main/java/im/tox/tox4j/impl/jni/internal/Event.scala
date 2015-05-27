package im.tox.tox4j.impl.jni.internal

import scala.collection.mutable.ArrayBuffer

private[jni] object Event {
  private val INVALID_INDEX = -1
  private val EMPTY_CALLBACK = () => ()

  trait Id {
    /**
     * @return The index in the callbacks list.
     */
    private[internal] def value: Int

    /**
     * Reset the index to an invalid value.
     */
    private[internal] def reset(): Unit
  }

  private final class IdImpl(private var index: Int) extends Id {
    def value: Int = index

    def reset(): Unit = {
      index = INVALID_INDEX
    }
  }
}

/**
 * Function multiplexer to turn a collection of functions into one.
 *
 * This is a collection of nullary functions returning `Unit` (`() => Unit)`) and is itself also a nullary function
 * returning unit. It can be used to implement events where one can register multiple handlers and selectively
 * unregister them.
 */
private[jni] final class Event extends (() => Unit) {
  private val callbacks = new ArrayBuffer[() => Unit]

  /**
   * Register a callback to be called on [[apply]].
   *
   * The returned [[Event.Id]] should be considered a linear value. It should only be owned by a single owner and never
   * shared.
   *
   * @param callback A [[Runnable]] instance to be called.
   * @return An [[Event.Id]] that can be used to [[apply]] the callback again.
   */
  def +=(callback: () => Unit): Event.Id = {
    callbacks += callback
    new Event.IdImpl(callbacks.size - 1)
  }

  /**
   * Unregister a callback. Requires an [[Event.Id]] from [[+=]].
   *
   * After calling this method, the [[Event.Id]] should be considered consumed. Removing the same event handler twice
   * may result in erroneous behaviour. In particular, if between the two [[-=]] calls there is a [[+=]] call, the event
   * ID may have been reused, and the second call will remove the newly added handler.
   *
   * @param id The callback id object.
   */
  def -=(id: Event.Id): Unit = {
    val index = id.value
    if (index != Event.INVALID_INDEX) {
      id.reset()
      callbacks(index) = Event.EMPTY_CALLBACK
      while (callbacks.nonEmpty && callbacks.last == Event.EMPTY_CALLBACK) {
        callbacks.remove(callbacks.size - 1)
      }
    }
  }

  /**
   * Invoke all callbacks.
   */
  override def apply(): Unit = callbacks.foreach(_())
}
