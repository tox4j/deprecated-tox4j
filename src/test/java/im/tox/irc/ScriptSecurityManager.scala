/**
 * Originally from https://github.com/OlegYch/multibot
 */
package im.tox.irc

import java.io.{ File, FilePermission }
import java.net.{ NetPermission, SocketPermission }
import java.security.{ Permission, SecurityPermission }
import java.util.PropertyPermission

object ScriptSecurityManager extends SecurityManager {
  System.setProperty("actors.enableForkJoin", false.toString)

  private val lock = new Object
  @volatile private var sm: Option[SecurityManager] = None
  @volatile private var activated = false

  def hardenPermissions[T](f: => T): T =
    lock.synchronized {
      try {
        activate()
        f
      } catch {
        case e: Exception => throw e
        case e: Throwable => e.printStackTrace(); sys.exit(-1)
      } finally {
        deactivate()
      }
    }

  override def checkPermission(perm: Permission): Unit = {
    if (activated) {
      try {
        deactivate()
        doChecks(perm)
      } finally {
        activate()
      }
    } else {
      if (sm.isDefined && sm.get != this) {
        sm.get.checkPermission(perm)
      }
    }
  }

  private def checkWhiteListedCallers(): Boolean = {
    new Throwable().getStackTrace.exists { element =>
      val name = element.getFileName
      // TODO: apply more robust checks
      List(
        "BytecodeWriters.scala",
        "Settings.scala",
        "PathResolver.scala",
        "JavaMirrors.scala",
        "ForkJoinPool.java",
        "Using.scala",
        "TimeZone.java"
      ).contains(name)
    }
  }

  private def checkWhiteListedPermissions(perm: Permission): Boolean = {
    Seq(
      "accessDeclaredMembers",
      "suppressAccessChecks",
      "createClassLoader",
      "accessClassInPackage.sun.reflect",
      "getStackTrace",
      "getClassLoader",
      "setIO",
      "getProtectionDomain",
      "setContextClassLoader",
      "getClassLoader",
      "accessClassInPackage.sun.misc",
      "loadLibrary.tox4j"
    ).contains(perm.getName) || perm.getName.startsWith("getenv")
  }

  private def checkFilePermission(perm: FilePermission): Boolean = {
    if (perm.getActions == "read") {
      if (!new File(perm.getName).exists()) {
        true
      } else {
        val cwd = sys.props("user.dir")

        val allowedFiles = Seq(
          ".*\\.jar",
          s"^$cwd/target/.*classes.*",
          s"^$cwd/target/cpp/bin/libtox4j.so"
        )
        allowedFiles.exists { allowed =>
          perm.getName.replaceAll("\\\\", "/").matches(allowed) ||
            new File(perm.getName).getAbsolutePath == new File(allowed).getAbsolutePath
        }
      }
    } else {
      false
    }
  }

  private def checkSpecificPermissions(perm: Permission): Boolean = {
    perm match {
      case perm: FilePermission =>
        checkFilePermission(perm)

      case perm: PropertyPermission =>
        perm.getActions == "read"

      case perm: SecurityPermission =>
        perm.getName.startsWith("getProperty.")

      case perm: NetPermission =>
        Seq(
          "specifyStreamHandler",
          "getProxySelector"
        ).contains(perm.getName)

      case perm: SocketPermission =>
        perm.getActions == "connect,resolve"

      case perm: RuntimePermission =>
        Seq(
          "readFileDescriptor"
        ).contains(perm.getName)

      case _ =>
        false
    }
  }

  private def doChecks(perm: Permission): Unit = {
    val allowed = Seq(
      checkWhiteListedCallers(),
      checkWhiteListedPermissions(perm),
      checkSpecificPermissions(perm)
    ).contains(true)

    if (!allowed) {
      val exception = new SecurityException(perm.toString)
      exception.printStackTrace()
      throw exception
    }
  }

  private def deactivate(): Unit = {
    activated = false
    if (System.getSecurityManager == this) {
      sm.foreach(System.setSecurityManager)
    }
  }

  private def activate(): Unit = {
    if (System.getSecurityManager != this) {
      sm = Option(System.getSecurityManager)
      System.setSecurityManager(this)
    }
    activated = true
  }
}
