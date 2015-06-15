package im.tox.tox4j

import java.io.{ Closeable, IOException, InputStream, OutputStream }
import java.net.{ InetAddress, ServerSocket, Socket }

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.SocksServer.{ FIRST_PORT, LAST_PORT, logger }
import im.tox.tox4j.annotations.NotNull
import org.junit.Assert.assertEquals
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

object SocksServer {
  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private val FIRST_PORT = 8000
  private val LAST_PORT = 8999

  /**
   * Spawn a proxy server in a thread and pass it to the function.
   *
   * @param function  The function to call ot the { @link SocksServer} object.
   * @tparam R        The return type of the function.
   * @return The result of calling the function on the server object.
   */
  @throws[IOException]("When a network error occurs")
  @throws[InterruptedException]("On unexpected thread interrupts")
  def withServer[R](function: SocksServer => R): R = {
    val server = new SocksServer
    val thread = new Thread(server)
    thread.start()
    try {
      function.apply(server)
    } finally {
      server.close()
      thread.join()
    }
  }
}

/**
 * Create a simple SOCKS5 server on a port between [[FIRST_PORT]] and [[LAST_PORT]].
 */
@throws[IOException]("If no port could be bound")
final class SocksServer extends Closeable with Runnable {
  private val server = connectAvailablePort()
  private val threads = new ArrayBuffer[Thread]
  private val sockets = new ArrayBuffer[Socket]
  private var running = true
  private var accepted = 0

  @throws[IOException]
  private def connectAvailablePort(): ServerSocket = {
    var lastException: IOException = null
    var socket: ServerSocket = null

    (FIRST_PORT to LAST_PORT) find { port =>
      try {
        socket = new ServerSocket(port)
        true
      } catch {
        case e: IOException =>
          lastException = e
          false
      }
    }

    if (lastException != null) {
      throw lastException
    }

    socket
  }

  @throws[IOException]
  override def close(): Unit = {
    running = false
    server.close()
  }

  def getPort: Int = server.getLocalPort
  def getAddress: String = server.getInetAddress.getHostAddress

  override def run(): Unit = {
    try {
      while (running) {
        val socket = server.accept
        sockets += socket
        val thread = new Thread(new Runnable() {
          def run(): Unit = {
            val input = socket.getInputStream
            val output = socket.getOutputStream
            try {
              greeting(input, output)
            } catch {
              case e: IOException =>
                logger.error("Exception", e)
            } finally {
              if (input != null) input.close()
              if (output != null) output.close()
            }
          }
        })
        thread.start()
        threads += thread
        accepted += 1
      }
    } catch {
      case abort: IOException =>
        running = false
        try {
          server.close()
          sockets.foreach(_.close())
          threads.foreach(_.join())
        } catch {
          case error: Throwable =>
            logger.error("Exception", error)
        }
    }
  }

  private def greeting(input: InputStream, output: OutputStream): Unit = {
    assertEquals(0x05, input.read)
    val numAuthenticationMethods = input.read
    val authenticationMethods = new Array[Int](numAuthenticationMethods)
    for (i <- 0 until numAuthenticationMethods) {
      authenticationMethods(i) = input.read
    }

    if (!authenticationMethods.contains(0x00)) {
      throw new IOException("Client did not support any of our authentication methods")
    }

    output.write(0x05)
    output.write(0x00)
    connection(input, output)
  }

  private def connection(@NotNull input: InputStream, @NotNull output: OutputStream): Unit = {
    assertEquals(0x05, input.read)
    val command = input.read
    assertEquals(0x00, input.read)

    val address =
      input.read match {
        case 0x01 =>
          val address4 = new Array[Byte](4)
          assertEquals(address4.length, input.read(address4))
          InetAddress.getByAddress(address4)
        case 0x03 =>
          val domain = new Array[Byte](input.read)
          assertEquals(domain.length, input.read(domain))
          InetAddress.getByName(new String(domain))
        case 0x04 =>
          val address6 = new Array[Byte](16)
          assertEquals(address6.length, input.read(address6))
          InetAddress.getByAddress(address6)
        case _ =>
          throw new IOException("Unsupported address type")
      }

    val portBytes = new Array[Byte](2)
    assertEquals(2, input.read(portBytes))
    val port = ((portBytes(0) & 0xff) << 8) | (portBytes(1) & 0xff)

    command match {
      case 0x01 => establishStream(input, output, address, port)
      case 0x02 => throw new IOException("TCP/IP port binding not supported")
      case 0x03 => throw new IOException("Associating UDP port not supported")
      case _    => throw new IOException("Unknown command: " + command)
    }
  }

  private def establishStream(input: InputStream, output: OutputStream, address: InetAddress, port: Int): Unit = {
    output.write(0x05)
    try {
      val target = new Socket(address, port)
      sockets += target
      pipeStream(input, output, address, port, target)
    } catch {
      case e: IOException if e.getMessage == "Network is unreachable" =>
        output.write(0x03)
    }
  }

  private def pipeStream(input: InputStream, output: OutputStream, address: InetAddress, port: Int, target: Socket): Unit = {
    output.write(0x00)
    output.write(0x00)

    val addressBytes = address.getAddress
    if (addressBytes.length == 4) {
      output.write(0x01)
    } else {
      assertEquals(16, addressBytes.length)
      output.write(0x04)
    }

    output.write(addressBytes)
    output.write(port >> 8)
    output.write(port & 0xff)

    val targetInput = target.getInputStream
    val targetOutput = target.getOutputStream

    val inputThread = new Thread(new Runnable() {
      def run(): Unit = {
        try {
          var inByte = targetInput.read
          while (inByte != -1) {
            output.write(inByte)
            inByte = targetInput.read
          }
        } catch {
          case ignored: IOException =>
        }
      }
    })

    inputThread.start()
    threads += inputThread

    try {
      var outByte = input.read
      while (outByte != -1) {
        targetOutput.write(outByte)
        outByte = input.read
      }
    } catch {
      case ignored: IOException =>
    }
  }

  def getAccepted: Int = {
    accepted
  }
}
