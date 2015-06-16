package sbt.tox4j.lint

import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.tox4j.Tox4jBuildPlugin

object Scapegoat extends Tox4jBuildPlugin {

  object Keys

  // Enable checkstyle.
  override val moduleSettings = Seq(
    scapegoatVerbose := false,
    scapegoatDisabledInspections := Seq(
      // Disable method name inspection, since we use things like name_=.
      "MethodNames",
      // This is simply wrong. Case classes can be extended, but it's a bad idea, so
      // we would like to make all of them final. WartRemover checks this.
      "RedundantFinalModifierOnCaseClass"
    ),
    scapegoatIgnoredFiles := Seq(".*/target/.*.scala", ".*/im/tox/tox4j/impl/jni/.*Impl\\.scala")
  )

}
