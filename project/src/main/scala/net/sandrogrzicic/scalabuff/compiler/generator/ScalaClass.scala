package net.sandrogrzicic.scalabuff.compiler.generator

import java.io.File

/**
 * A generated Scala class. The path is relative.
 */
final case class ScalaClass(body: String, path: String, file: String) {
  assert(path.endsWith(File.separator), "path must end with a " + File.separator)
  assert(!file.contains(File.separator), "file name must not contain a " + File.separator)
}
