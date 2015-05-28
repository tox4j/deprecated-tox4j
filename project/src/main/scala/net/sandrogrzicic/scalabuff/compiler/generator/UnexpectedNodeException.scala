package net.sandrogrzicic.scalabuff.compiler.generator

import net.sandrogrzicic.scalabuff.compiler.nodes.Node

/**
 * Thrown when a Node occurs in an unexpected location in the tree.
 */
class UnexpectedNodeException(node: Node, parentNode: Node = null) extends GenerationFailureException(
  "Unexpected child node " + node + (if (parentNode ne null) "found in " + parentNode else "")
)
