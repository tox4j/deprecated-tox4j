package im.tox.tox4j.core

import im.tox.tox4j.core.options.{ ProxyOptions, ToxOptions }
import im.tox.tox4j.impl.ToxCoreImpl

import scala.collection.mutable.ArrayBuffer

object ToxCoreFactory {

  private final val toxes = new ArrayBuffer[ToxCore]

  private def make(options: ToxOptions): ToxCore = {
    new ToxCoreImpl(options)
  }

  def destroyAll(): Unit = {
    toxes.foreach(_.close())
    toxes.clear()
    System.gc()
  }

  def apply(options: ToxOptions): ToxCore = {
    val tox = make(options)
    toxes += tox
    tox
  }

  def withTox[R](options: ToxOptions)(f: ToxCore => R): R = {
    val tox = make(options)
    try {
      f(tox)
    } finally {
      tox.close()
    }
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean, proxy: ProxyOptions.Type)(f: ToxCore => R): R = {
    withTox(new ToxOptions(ipv6Enabled, udpEnabled, proxy))(f)
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean)(f: ToxCore => R): R = {
    withTox(new ToxOptions(ipv6Enabled, udpEnabled))(f)
  }

  def withTox[R](f: ToxCore => R): R = {
    withTox(ipv6Enabled = true, udpEnabled = true)(f)
  }

  def withToxes[R](count: Int)(f: ToxList => R): R = {
    val toxes = new ToxList(() => { this(new ToxOptions) }, count)
    try {
      f(toxes)
    } finally {
      toxes.close()
    }
  }

}
