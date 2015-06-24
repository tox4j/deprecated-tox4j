package sbt.tox4j.lint

import java.net.URLClassLoader

import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport._

import scala.collection.JavaConversions._

object WartRemover extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private def asFile(url: URL): List[File] = {
    val path = url.getPath
    val end = path.indexOf('!')
    val jarPath =
      if (end == -1) {
        path
      } else {
        path.substring(0, end)
      }

    List(new File(new URI(jarPath)))
  }

  private def filterWartLibraries(classpath: Seq[File]): Seq[File] = {
    val loader = new URLClassLoader(Path.toURLs(classpath))
    loader.getResources("warts.ini").toSeq.flatMap(asFile)
  }

  // Enable wart removers.
  override val projectSettings = Seq(
    scalacOptions ++= {
      val pluginClasspath = update.value
        .matching(configurationFilter(Configurations.CompilerPlugin.name))
      filterWartLibraries(pluginClasspath) map { cp =>
        s"-P:wartremover:cp:${cp.toURI}"
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
