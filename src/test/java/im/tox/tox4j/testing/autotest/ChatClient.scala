package im.tox.tox4j.testing.autotest

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.callbacks.ToxEventAdapter
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.{ ToxCore, ToxCoreConstants }
import im.tox.tox4j.exceptions.ToxException
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

class ChatClient(val selfName: String, val expectedFriendName: String) extends ToxEventAdapter {

  val logger = Logger(LoggerFactory.getLogger(getOuterClass(getClass)))

  private def getOuterClass(clazz: Class[_]): Class[_] = {
    Option(clazz.getEnclosingClass) match {
      case None            => clazz
      case Some(enclosing) => enclosing
    }
  }

  private val tasks = new ArrayBuffer[(ToxCore => Any, Array[StackTraceElement])]

  protected def addTask(task: ToxCore => Any): Unit = {
    val creationTrace = Thread.currentThread.getStackTrace.tail
    tasks += ((task, creationTrace))
  }

  private var isDone: Boolean = false
  private var chatting: Boolean = true

  def isChatting: Boolean = chatting
  def finish(): Unit = chatting = false

  var expectedFriendAddress: Array[Byte] = null
  def expectedFriendPublicKey: Array[Byte] = expectedFriendAddress.slice(0, ToxCoreConstants.PUBLIC_KEY_SIZE)

  protected def isAlice = selfName == "Alice"
  protected def isBob = selfName == "Bob"

  def isRunning: Boolean = !isDone

  def exit(): Unit = this.isDone = true

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
    val iterationTasks = tasks.clone()
    tasks.clear()
    for (task <- iterationTasks) {
      try {
        task._1.apply(tox)
      } catch {
        case e: ToxException[_] =>
          throw assembleStackTrace(e, task._2)
      }
    }
  }
}
