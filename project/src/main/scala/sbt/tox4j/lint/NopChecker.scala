package sbt.tox4j.lint

import org.scalastyle.{ScalariformChecker, ScalastyleError}

import scalariform.parser.CompilationUnit

final class NopChecker extends ScalariformChecker {

  override protected val errorKey: String = "nop"

  override def verify(ast: CompilationUnit): List[ScalastyleError] = {
    Nil
  }

}
