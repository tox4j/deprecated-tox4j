package im.tox.tox4j.core

import im.tox.tox4j.core.enums.ToxProxyType
import im.tox.tox4j.impl.ToxCoreJni

import scala.collection.mutable.ArrayBuffer

object ToxCoreFactory {

  private final val toxes = new ArrayBuffer[ToxCore]

  private def make(options: ToxOptions, data: Array[Byte]): ToxCore = {
    new ToxCoreJni(options, data)
  }

  def destroyAll() {
    toxes.foreach(_.close())
    toxes.clear()
    System.gc()
  }

  def apply(options: ToxOptions, data: Array[Byte]): ToxCore = {
    val tox = make(options, data)
    toxes += tox
    tox
  }

  def withTox[R](options: ToxOptions, data: Array[Byte])(f: ToxCore => R): R = {
    val tox = make(options, data)
    try {
      f(tox)
    } finally {
      tox.close()
    }
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean, proxyType: ToxProxyType, proxyAddress: String, proxyPort: Int)(f: ToxCore => R): R = {
    withTox(new ToxOptions(ipv6Enabled, udpEnabled, proxyType, proxyAddress, proxyPort), null)(f)
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean)(f: ToxCore => R): R = {
    withTox(new ToxOptions(ipv6Enabled, udpEnabled), null)(f)
  }

  def withTox[R](f: ToxCore => R): R = {
    withTox(ipv6Enabled = true, udpEnabled = true)(f)
  }

  def withToxes[R](count: Int)(f: ToxList => R): R = {
    val toxes = new ToxList(() => { this(new ToxOptions, null) }, count)
    try {
      f(toxes)
    } finally {
      toxes.close()
    }
  }

}
