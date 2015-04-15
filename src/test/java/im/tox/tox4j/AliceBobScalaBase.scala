package im.tox.tox4j

import im.tox.tox4j.core.callbacks.ToxEventAdapter
import im.tox.tox4j.exceptions.ToxException

object AliceBobScalaBase {

  abstract class TaskBase[T] {
    protected var creationTrace: Array[StackTraceElement] = null
    private var wakeUp: Long = 0

    protected def sleep(iterations: Int): Unit = {
      wakeUp = iterations
    }

    def sleeping: Boolean = {
      wakeUp > 0
    }

    def slept(): Unit = {
      wakeUp -= 1
    }

    @throws[ToxException[_]]
    def perform(tox: T): Unit
  }

  class ChatClient extends ToxEventAdapter {

    private var isDone: Boolean = false

    def isRunning: Boolean = {
      !isDone
    }

    @throws[InterruptedException]
    def done(): Unit = {
      this.isDone = true
    }

  }

}

class AliceBobScalaBase extends ToxCoreImplTestBase
