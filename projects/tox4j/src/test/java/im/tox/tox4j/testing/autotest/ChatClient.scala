package im.tox.tox4j.testing.autotest

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.callbacks.ToxEventAdapter
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.{ToxCore, ToxCoreConstants}
import im.tox.tox4j.exceptions.ToxException
import org.slf4j.LoggerFactory

class ChatClient(val selfName: String, val expectedFriendName: String) extends ToxEventAdapter {

  val logger = Logger(LoggerFactory.getLogger(getOuterClass(getClass)))

  private case class State(
    tasks: Seq[(ToxCore => Any, Array[StackTraceElement])] = Nil,
    done: Boolean = false,
    chatting: Boolean = true
  ) {
    def addTask(task: ToxCore => Any): State = {
      val creationTrace = Thread.currentThread.getStackTrace
      copy((task, creationTrace.slice(2, creationTrace.length)) +: tasks)
    }
  }

  private def getOuterClass(clazz: Class[_]): Class[_] = {
    Option(clazz.getEnclosingClass) match {
      case None            => clazz
      case Some(enclosing) => enclosing
    }
  }

  private var state = State() // scalastyle:ignore var.field

  protected def addTask(task: ToxCore => Any): Unit = {
    state = state.addTask(task)
  }

  def isChatting: Boolean = state.chatting
  def finish(): Unit = state = state.copy(chatting = false)

  var expectedFriendAddress: Array[Byte] = null
  def expectedFriendPublicKey: Array[Byte] = expectedFriendAddress.slice(0, ToxCoreConstants.PUBLIC_KEY_SIZE)

  protected def isAlice = selfName == "Alice"
  protected def isBob = selfName == "Bob"

  def isRunning: Boolean = !state.done

  def exit(): Unit = state = state.copy(done = true)

  def setup(tox: ToxCore): Unit = {}

  protected def debug(message: String): Unit = {
    logger.info(s"[${Thread.currentThread.getId}] $selfName: $message")
  }

  override def selfConnectionStatus(connectionStatus: ToxConnection): Unit = {
    if (connectionStatus != ToxConnection.NONE) {
      debug("is now connected to the network")
    } else {
      debug("is now disconnected from the network")
    }
  }

  private def assembleStackTrace(e: ToxException[_], creationTrace: Array[StackTraceElement]): ToxException[_] = {
    // The stack until the performTasks method call.
    val untilPerformTasks = e.getStackTrace
      .reverse
      .dropWhile { callSite =>
        !((callSite.getClassName == classOf[ChatClient].getName) &&
          (callSite.getMethodName == "performTasks"))
      }
      .reverse

    // After that, add the task creation trace, minus the "addTask" method.
    val trace = untilPerformTasks ++ creationTrace

    // Put the assembled trace into the exception and return it.
    e.setStackTrace(trace)

    e
  }

  def performTasks(tox: ToxCore): Unit = {
    val state = this.state
    this.state = state.copy(tasks = Nil)
    for (task <- state.tasks.reverse) {
      try {
        task._1.apply(tox)
      } catch {
        case e: ToxException[_] =>
          throw assembleStackTrace(e, task._2)
      }
    }
  }

}
