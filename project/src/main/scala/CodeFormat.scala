import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, scalariformSettings}
import sbt._

import scala.language.postfixOps
import scalariform.formatter.preferences._

object CodeFormat extends Plugin {

  override val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
    )

}
