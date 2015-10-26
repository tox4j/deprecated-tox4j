package sbt.tox4j

import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, scalariformSettings}
import sbt.{AutoPlugin, PluginTrigger}

import scalariform.formatter.preferences._

object CodeFormat extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override val projectSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  )

}
