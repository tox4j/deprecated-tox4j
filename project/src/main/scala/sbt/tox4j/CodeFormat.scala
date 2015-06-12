package sbt.tox4j

import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, scalariformSettings}

import scala.language.postfixOps
import scalariform.formatter.preferences._

object CodeFormat extends Tox4jBuildPlugin {

  override val moduleSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
  )

}
