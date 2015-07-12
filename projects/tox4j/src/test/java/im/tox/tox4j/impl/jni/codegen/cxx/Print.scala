package im.tox.tox4j.impl.jni.codegen.cxx

import im.tox.tox4j.impl.jni.codegen.cxx.Ast._
import org.apache.commons.lang3.StringEscapeUtils
import gnieh.pp._

object Print {

  private def flatten(docs: Seq[Doc], sep: Doc = empty): Doc = {
    docs match {
      case Nil => empty
      case head +: tail =>
        tail.foldLeft(head)((a, b) => a :: sep :: b)
    }
  }

  private def printSeq[T](printT: T => Doc, nodes: Seq[T], sep: Doc = empty): Doc = {
    flatten(nodes.map(printT), sep)
  }

  /**
   * Types.
   */
  private def printTypeInner(ty: Type): Doc = {
    ty match {
      case Typename(name) => text(name) :: space
      case Pointer(inner) => printTypeInner(inner) :: "*"
    }
  }

  def printType(ty: Type, spacing: Doc = empty): Doc = {
    ty match {
      case Typename(name) => text(name) :: spacing
      case _              => printTypeInner(ty)
    }
  }

  /**
   * Pre-processing directives.
   */
  def printPreproc(pp: Preproc): Doc = {
    pp match {
      case Include(header)      => "#include \"" :: header :: "\""
      case Ifdef(name)          => "#ifdef " :: name
      case Comment(text)        => "// " :: text
      case Endif                => "#endif"
      case MacroCall(expr)      => printExpr(expr)
      case MacroFun(init, body) => printExpr(init) :|: printStmt(body)
    }
  }

  /**
   * Statements.
   */
  // scalastyle:ignore cyclomatic.complexity
  def printStmt(stmt: Stmt): Doc = {
    stmt match {
      case Switch(cond, CompoundStmt(cases)) =>
        "switch (" :: printExpr(cond) :: nest(2)(")" :|:
          "{" :|:
          printSeq[Stmt](printStmt, cases, line) :|:
          "}") ::
          line
      case Switch(cond, body)  => sys.error("Invalid switch-body: " + body)
      case Case(expr, body)    => "case" :+: printExpr(expr) :: ":" :+: printStmt(body)
      case Default(body)       => "default:" :+: printStmt(body)
      case Break               => "break;"
      case Return(expr)        => "return" :: expr.fold(empty)(expr => space :: printExpr(expr)) :: ";"
      case ExprStmt(expr)      => printExpr(expr) :: ";"
      case CompoundStmt(stmts) => nest(2)("{" :|: printSeq[Stmt](printStmt, stmts)) :|: "}"
      case stmt: Oneliner      => printSeq[Stmt](printStmt, stmt.stmts, space)
      case pp: Preproc         => printPreproc(pp)
    }
  }

  /**
   * Expressions.
   */
  def printExpr(expr: Expr): Doc = {
    expr match {
      case Identifier(name)      => name
      case IntegerLiteral(value) => value
      case StringLiteral(value)  => "\"" :: StringEscapeUtils.escapeJava(value) :: "\""
      case FunCall(callee, args) => printExpr(callee) :+: "(" :: printSeq(printExpr, args, "," :: space) :: ")"
      case LeftShift(lhs, rhs)   => printExpr(lhs) :+: "<<" :+: printExpr(rhs)
    }
  }

  /**
   * Declarations.
   */
  def printDecl(decl: Decl): Doc = {
    decl match {
      case Fun(ty, name, params, body) =>
        printType(ty) :: name :: "(" :: printSeq(printDecl, params, "," :: space) :: ")" :: printStmt(body)

      case TemplateFun(tparams, targs, Fun(ty, name, params, body)) =>
        "template<" :: flatten(tparams.map("typename" :+: _)) :: ">" :|:
          printType(ty) :|:
          name :: "<" :: flatten(targs.map(printType(_))) :: "> (" :: printSeq(printDecl, params, "," :: space) :: ")" :|:
          printStmt(body)

      case Param(ty, name) => printType(ty, space) :: name
      case pp: Preproc     => printPreproc(pp)
    }
  }

}
