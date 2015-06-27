package im.tox.tox4j.lint

import org.brianmckenna.wartremover.{ WartTraverser, WartUniverse }

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
object OptionsClasses extends WartTraverser {

  def errorDefaultArguments: String = "Non-ADT options case class must have default arguments for all parameters"
  def errorCaseClass: String = "Options classes must be case classes"
  def errorBadParent(parentName: String): String = s"Options classes can only inherit from options traits: $parentName not allowed"

  private def isTox4jType(symbolName: String): Boolean = {
    Seq("av", "core") exists { pkg =>
      symbolName.startsWith(s"im.tox.tox4j.$pkg.options")
    }
  }

  private def getConstructorParams(u: WartUniverse)(tree: u.universe.Tree) = {
    import u.universe._
    tree match {
      case DefDef(mods, termNames.CONSTRUCTOR, tparams, List(vparams), tpt, rhs) =>
        vparams
      case _ =>
        Nil
    }
  }

  private def inheritsFromTox4jType(u: WartUniverse)(parents: List[u.universe.Tree]): Boolean = {
    parents.exists { parent =>
      isTox4jType(parent.tpe.typeSymbol.fullName)
    }
  }

  private def checkCaseClass(u: WartUniverse)(parents: List[u.universe.Tree], body: List[u.universe.Tree]) = {
    if (inheritsFromTox4jType(u)(parents)) {
      Nil
    } else {
      for {
        decl <- body
        param <- getConstructorParams(u)(decl)
        if param.rhs.isEmpty
      } yield {
        param.pos
      }
    }
  }

  private def checkTree(u: WartUniverse)(tree: u.universe.Tree) = {
    import u.universe._

    val allowedParents = Array(
      "java.lang.Object",
      "scala.Product",
      "scala.Serializable"
    )

    tree match {
      case ClassDef(mods, typeName, _, Template(parents, self, body)) if isTox4jType(tree.symbol.fullName) =>

        parents.foreach { parent =>
          val typeName = parent.tpe.typeSymbol.fullName
          if (!isTox4jType(typeName)) {
            if (!allowedParents.contains(typeName)) {
              u.error(tree.pos, errorBadParent(typeName))
            }
          }
        }

        if (mods.hasFlag(Flag.CASE)) {
          checkCaseClass(u)(parents, body) foreach { pos =>
            u.error(pos, errorDefaultArguments)
          }
        } else if (!mods.hasFlag(Flag.TRAIT)) {
          u.error(tree.pos, errorCaseClass)
        }

      case _ =>
    }
  }

  def apply(u: WartUniverse): u.universe.Traverser = {
    new u.universe.Traverser {
      override def traverse(tree: u.universe.Tree): Unit = {
        checkTree(u)(tree)
        super.traverse(tree)
      }
    }
  }

}
