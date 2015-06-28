package sbt.tox4j.lint

import java.io.InputStream
import java.net.URLClassLoader
import java.util.Properties

import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport._

import scala.collection.JavaConverters._

object WartLoader extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private def withInputStream[R](url: URL)(f: InputStream => R): R = {
    val inputStream = url.openStream()
    try {
      f(inputStream)
    } finally {
      inputStream.close()
    }
  }

  private def loadProperties(propertiesFile: URL): Properties = {
    withInputStream(propertiesFile) { input =>
      val properties = new Properties()
      properties.load(input)
      properties
    }
  }

  private def asFile(propertiesFile: URL): (URI, Seq[String]) = {
    val path = propertiesFile.getPath
    val end = path.indexOf('!')
    val jarPath =
      if (end == -1) {
        path
      } else {
        path.substring(0, end)
      }

    val properties = loadProperties(propertiesFile)
    val customWarts = properties.asScala.values.toSeq

    (new URI(jarPath), customWarts)
  }

  private def filterWartLibraries(classpath: Seq[File]): Seq[(URI, Seq[String])] = {
    val loader = new URLClassLoader(Path.toURLs(classpath))
    loader.getResources("warts.properties").asScala.map(asFile).toSeq
  }

  // Enable wart removers.
  override val projectSettings = Seq(
    scalacOptions in (Compile, compile) ++= {
      val pluginClasspath = update.value.matching(configurationFilter(Configurations.CompilerPlugin.name))
      filterWartLibraries(pluginClasspath) flatMap {
        case (cp, customWarts) =>
          customWarts.map { clazz =>
            "-P:wartremover:traverser:" + clazz
          } :+ "-P:wartremover:cp:" + cp
      }
    },
    wartremoverErrors in (Compile, compile) := Warts.allBut(
      Wart.DefaultArguments,
      Wart.NoNeedForMonad, // https://github.com/puffnfresh/wartremover/issues/179
      Wart.NonUnitStatements,
      Wart.Throw, // https://github.com/puffnfresh/wartremover/issues/182
      Wart.Var
    ),
    wartremoverErrors in (Test, compile) := Warts.allBut(
      Wart.Any,
      Wart.AsInstanceOf,
      Wart.DefaultArguments,
      Wart.IsInstanceOf,
      Wart.NoNeedForMonad, // https://github.com/puffnfresh/wartremover/issues/179
      Wart.NonUnitStatements,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Throw,
      Wart.Var
    )
  )

}
