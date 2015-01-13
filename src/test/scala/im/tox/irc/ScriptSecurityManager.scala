/**
 * Originally from https://github.com/OlegYch/multibot
 */
package im.tox.irc

import java.io.{File, FilePermission}
import java.net.{NetPermission, SocketPermission}
import java.security.{Permission, SecurityPermission}
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
      }
      catch {
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

  private def doChecks(perm: Permission): Unit = {
    if (new Throwable().getStackTrace.exists { element =>
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
    }) return

    if (Seq(
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
    ).contains(perm.getName)) return

    if (perm.getName.startsWith("getenv"))
      return

    perm match {
      case perm: FilePermission =>
        if (perm.getActions == "read") {
          if (!new File(perm.getName).exists())
            return

          val cwd = sys.props("user.dir")

          val allowedFiles = Seq(
            ".*\\.jar",
            s"^$cwd/target/.*classes.*",
            s"^$cwd/target/cpp/bin/libtox4j.so"
          )
          if (allowedFiles.exists { allowed =>
            perm.getName.replaceAll("\\\\", "/").matches(allowed) ||
              new File(perm.getName).getAbsolutePath == new File(allowed).getAbsolutePath
          }) return
        }

      case perm: PropertyPermission =>
        if (perm.getActions == "read")
          return

      case perm: SecurityPermission =>
        if (perm.getName.startsWith("getProperty."))
          return

      case perm: NetPermission =>
        if (Seq(
          "specifyStreamHandler",
          "getProxySelector"
        ).contains(perm.getName)) return

      case perm: SocketPermission =>
        if (perm.getActions == "connect,resolve")
          return

      case perm: RuntimePermission =>
        if (Seq(
          "readFileDescriptor"
        ).contains(perm.getName)) return

      case _ =>
    }

    val exception = new SecurityException(perm.toString)
    exception.printStackTrace()
    throw exception
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
