/**
 * Originally from https://github.com/OlegYch/multibot
 */
package im.tox.irc

import java.net.URL

import org.jibble.pircbot.{NickAlreadyInUseException, PircBot}

import java.io.{PrintStream, ByteArrayOutputStream}
import com.google.common.cache.{RemovalNotification, RemovalListener, CacheLoader, CacheBuilder}
import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.collection.mutable
import scala.tools.nsc.interpreter.IMain

object EvalBot extends PircBot {
  private val BOT_NAME = "tox4j"
  private val BOT_MSG = BOT_NAME + ":"
  private val MAX_LINES = 10
  private val ADMINS = Seq("pippijn")
  private val CHANNELS = Map("irc.freenode.net" -> Seq("#tox4j"))
  private val lastCode = new mutable.HashMap[String, String]

  def main(args: Array[String]) = {
    setName(BOT_NAME)
    setVerbose(true)
    setEncoding("UTF-8")
    tryConnect()
  }

  @tailrec
  private def tryConnect(): Unit =
    try {
      connect()
    } catch {
      case e: NickAlreadyInUseException =>
        setName(getName + "_")
        tryConnect()
      case e: Exception =>
        e.printStackTrace()
        sys.exit(-1)
    }

  private def connect(): Unit =
    CHANNELS foreach { case (server, channels) =>
      connect(server)
      channels foreach joinChannel
    }

  override def onDisconnect(): Unit =
    while (true) {
      try {
        tryConnect()
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Thread.sleep(10000)
      }
    }


  override def handleLine(line: String): Unit = {
    import scala.concurrent.{Promise, Future}
    import scala.util.Success
    import scala.concurrent.ExecutionContext.Implicits.global

    val timeout = Promise[Boolean]()
    try {
      Future {
        scala.concurrent.blocking(Thread.sleep(1000 * 60))
        timeout.tryComplete(Success(true))
      }
      for (timeout <- timeout.future) {
        if (timeout) {
          println(s"Timed out evaluating $line")
          sys.exit(1)
        }
      }
      super.handleLine(line)
      scalaInt.cleanUp()
    } catch {
      case e: Exception => throw e
      case e: Throwable => e.printStackTrace(); sys.exit(-1)
    } finally {
      timeout.tryComplete(Success(false))
    }
  }


  private def discardedMessage(count: Int) = {
    s"[+ $count discarded line${if (count == 1) "" else "s"}]"
  }

  private object Cmd {
    def unapply(s: String) =
      if (s.contains(' '))
        Some(s.split(" ", 2).toList)
      else
        None
  }

  private def evaluateCode(code: String, channel: String) = {
    sendLines(channel, scalaInterpreter(channel) { (si, output) =>
      import scala.tools.nsc.interpreter.Results._
      lastCode += ((channel, code))
      si interpret code match {
        case Success =>
          output.toString
            .replaceAll("(?m:^res[0-9]+: )", "")
            .replaceAll("^<console>:[0-9]+: ", "")
        case Error =>
          def removeStackTrace(msg: String): String = {
            val split = msg.split("\n")
            if (split.length > split.filterNot(_.startsWith("\tat ")).length) {
              split(0) + " " + discardedMessage(split.length - 1)
            } else {
              msg
            }
          }
          removeStackTrace(
            output.toString
              .replaceAll("^<console>:[0-9]+: ", "")
          )
        case Incomplete =>
          "error: unexpected EOF found, incomplete expression"
      }
    })
  }

  private val Replacement = """^!replace (.*) with (.*)""".r

  override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) =
    onMessage(sender, sender, login, hostname, message)

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) = {
    def withPrevious(block: String => Unit) = {
      try {
        block(lastCode(channel))
      } catch {
        case _: NoSuchElementException =>
          sendMessage(channel, "no previous code")
      }
    }

    message match {
      case Cmd(BOT_MSG :: m :: Nil) if ADMINS contains sender =>
        m match {
          case "quit" => sys.exit(0)
          case _ => sendMessage(channel, "unknown command: " + m)
        }

      case Cmd("!" :: m :: Nil) =>
        evaluateCode(m, channel)

      case Cmd("!type" :: m :: Nil) =>
        sendMessage(channel, scalaInterpreter(channel)((si, cout) => si.typeOfExpression(m).directObjectString))

      case Replacement(from, to) =>
        withPrevious { last =>
          evaluateCode(last.replaceAll(from, to), channel)
        }

      case "!show" =>
        withPrevious(sendMessage(channel, _))

      case "!reset" =>
        scalaInt invalidate channel

      case _ =>
    }
  }


  private val stdOut = System.out
  private val stdErr = System.err
  private val conOut = new ByteArrayOutputStream

  private def captureOutput[T](block: => T): T = try {
    val conOutStream = new PrintStream(conOut)
    System.setOut(conOutStream)
    System.setErr(conOutStream)
    Console.withOut(conOutStream) {
      Console.withErr(conOutStream) {
        block
      }
    }
  } finally {
    System.setOut(stdOut)
    System.setErr(stdErr)
    conOut.flush()
    conOut.reset()
  }

  private val scalaInt = interpreterCache(new CacheLoader[String, IMain] {
    override def load(key: String) = {
      val settings = new scala.tools.nsc.Settings(null)
      settings.usejavacp.value = true
      settings.deprecation.value = true
      settings.feature.value = false
      val si = new IMain(settings)

      val imports = List(
        "scala.collection._",

        "im.tox.client._",
        "im.tox.tox4j._",
        "im.tox.tox4j.core._",
        "im.tox.tox4j.core.callbacks._",
        "im.tox.tox4j.core.enums._",
        "im.tox.tox4j.core.exceptions._"
      )
      si.beQuietDuring {
        imports.foreach(i => si.interpret(s"import $i"))
      }
      si
    }
  })

  private def scalaInterpreter(channel: String)(f: (IMain, ByteArrayOutputStream) => String) = this.synchronized {
    val si = scalaInt.get(channel)
    ScriptSecurityManager.hardenPermissions(captureOutput {
      f(si, conOut)
    })
  }

  private def interpreterCache[K <: AnyRef, V <: AnyRef](loader: CacheLoader[K, V]) =
    CacheBuilder
      .newBuilder()
      .expireAfterAccess(1, TimeUnit.HOURS)
      .softValues()
      .maximumSize(CHANNELS.size + 5)
      .removalListener(new RemovalListener[K, V] {
      override def onRemoval(notification: RemovalNotification[K, V]) = {
        println(s"expired $notification")
      }
    }).build(loader)

  private def sendLines(channel: String, message: String) = {
    val lines = message.take(10 * 1024).split("\n").filter(!_.isEmpty)
    val output = lines.take(MAX_LINES)
    print(lines.take(100).mkString("\n"))

    output foreach { m =>
      val message =
        if (!m.isEmpty && m.charAt(0) == 13)
          m.substring(1)
        else
          m
      sendMessage(channel, message)
    }

    if (lines.length > output.length) {
      sendMessage(channel, discardedMessage(lines.length - output.length))
    }
  }

}
