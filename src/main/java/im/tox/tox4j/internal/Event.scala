package im.tox.tox4j.internal

import im.tox.tox4j.internal.Event.EmptyCallback

import scala.collection.mutable.ArrayBuffer

object Event {
  private val INVALID_INDEX = -1

  private object EmptyCallback extends Runnable {
    override def run(): Unit = {}
  }

  trait Id {
    /**
     * The index in the callbacks list.
     */
    def value: Int

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

final class Event extends Runnable {
  private val callbacks = new ArrayBuffer[Runnable]

  /**
   * Register a callback to be called on [[run]].
   *
   * @param callback A [[Runnable]] instance to be called.
   * @return An [[Event.Id]] that can be used to [[run]] the callback again.
   */
  def add(callback: Runnable): Event.Id = {
    callbacks += callback
    new Event.IdImpl(callbacks.size - 1)
  }

  /**
   * Unregister a callback. Requires an [[Event.Id]] from [[add]].
   *
   * @param id The callback id object.
   */
  def remove(id: Event.Id): Unit = {
    val index = id.value
    if (index != Event.INVALID_INDEX) {
      id.reset()
      callbacks(index) = EmptyCallback
      while (callbacks.nonEmpty && callbacks.last == EmptyCallback) {
        callbacks.remove(callbacks.size - 1)
      }
    }
  }

  /**
   * Invoke all callbacks.
   */
  def run(): Unit = {
    callbacks.foreach(_.run())
  }
}
