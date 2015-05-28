package net.sandrogrzicic.scalabuff.compiler

import java.io._
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

import net.sandrogrzicic.scalabuff.compiler.generator.{ Generator, ImportedSymbol, ScalaClass }
import net.sandrogrzicic.scalabuff.compiler.nodes.{ EnumStatement, ImportStatement, Message, Node, OptionValue, PackageStatement }

import scalariform.formatter.ScalaFormatter
import scalariform.formatter.preferences.{DoubleIndentClassDeclaration, AlignSingleLineCaseStatements, FormattingPreferences}

/**
 * ScalaBuff runtime.
 * @author Sandro Gržičić
 */
object ScalaBuff {

  private val defaultCharset: Charset = if (Charset.isSupported("utf-8")) Charset.forName("utf-8") else Charset.defaultCharset()

  final case class Settings(
    outputDirectory: File = new File("." + File.separatorChar),
    importDirectories: Seq[File] = Seq(new File(".")),
    stdout: Boolean = false,
    inputEncoding: Charset = defaultCharset,
    outputEncoding: Charset = defaultCharset,
    verbose: Boolean = false,
    extraVerbose: Boolean = false,
    generateJsonMethod: Boolean = false,
    targetScalaVersion: Option[String] = None
  )

  /**
   * Runs ScalaBuff on the specified file and returns the resulting Scala class.
   * If the encoding is not specified, it defaults to either UTF-8 (if available) or the platform default charset.
   */
  private def generateClass(file: File)(implicit settings: Settings) = {
    val tree = parse(file)
    val symbols = processImportSymbols(tree)
    Generator(tree, file.getName, symbols, settings.generateJsonMethod, settings.targetScalaVersion)
  }

  /**
   * Runs ScalaBuff on the specified input String and returns the output Scala class.
   */
  private def fromString(input: String, generateJsonMethod: Boolean = false, targetScalaVersion: Option[String] = None) = {
    Generator(Parser(input), "", Map(), generateJsonMethod, targetScalaVersion)
  }

  /**
   * Parse a protobuf file into Nodes.
   */
  private def parse(file: File)(implicit settings: Settings): List[Node] = {
    val reader = read(file)
    try {
      Parser(reader)
    } finally {
      reader.close()
    }
  }

  /**
   * Process "import" statements in a protobuf AST by scanning the imported
   * files and building a map of their exported symbols.
   */
  private def processImportSymbols(tree: List[Node])(implicit settings: Settings): Map[String, ImportedSymbol] = {
    def dig(name: String): List[(String, ImportedSymbol)] = {
      val tree = parse(searchPath(name).getOrElse { throw new IOException("Unable to import: " + name) })
      val packageName = tree.collectFirst {
        case OptionValue(key, value) if key == "java_package" => value.stripQuotes
      }.getOrElse("")
      val protoPackage = tree.collectFirst {
        case PackageStatement(`name`) => name
      }.getOrElse("")
      tree.collect {
        case Message(`name`, _)          => (name, ImportedSymbol(packageName, isEnum = false, protoPackage))
        case EnumStatement(`name`, _, _) => (name, ImportedSymbol(packageName, isEnum = true, protoPackage))
      }
    }
    tree.collect {
      case ImportStatement(name) => dig(name.stripQuotes)
    }.flatten.toMap
  }

  private val protoFileFilter = new FileFilter {
    def accept(filtered: File) = filtered.getName.endsWith(".proto")
  }

  private def findFiles(startAt: File): Seq[File] = {
    def recurse(src: File, seq: Seq[File] = Seq[File]()): Seq[File] = {
      src match {
        case e if !e.exists() =>
          println(Strings.INVALID_IMPORT_DIRECTORY + e); seq
        case f if f.isFile => seq :+ src
        case d             => seq ++ src.listFiles(protoFileFilter).toSeq.map(recurse(_)).foldLeft(Seq[File]())(_ ++ _)
      }
    }

    recurse(startAt)
  }

  private def searchPath(filename: String)(implicit settings: Settings): Option[File] = {
    val file = new File(filename)

    if (file.isAbsolute) {
      Option(file).filter(_.exists)
    } else {
      settings.importDirectories.map { folder =>
        new File(folder, filename)
      }.find(_.exists)
    }
  }

  private def verbosePrintln(msg: String)(implicit settings: Settings): Unit = {
    if (settings.verbose) {
      println(msg)
    }
  }

  /**
   * Runner: Runs ScalaBuff on the specified resource path(s).
   *
   * @return success: if true, no errors were encountered.
   */
  def run(rawSettings: Settings, protoFiles: Set[File]): Boolean = {
    implicit val parsedSettings = rawSettings

    val success = new AtomicBoolean(true)

    for (file <- protoFiles.par) {
      verbosePrintln("Processing: " + file.getAbsolutePath)
      try {
        val scalaClass = generateClass(file)
        try {
          write(scalaClass)
        } catch {
          // just print the error and continue processing
          case io: IOException =>
            success.set(false)
            println(Strings.CANNOT_WRITE_FILE + scalaClass.path + scalaClass.file + ".scala")
        }
      } catch {
        // just print the error and continue processing
        case pf: ParsingFailureException =>
          success.set(false)
          println(pf.getMessage)
        case io: IOException =>
          success.set(false)
          println(Strings.CANNOT_ACCESS_RESOURCE + file.getAbsolutePath)
      }
    }
    success.get()
  }

  /**
   * Returns a new Reader based on the specified File and Charset.
   */
  private def read(file: File)(implicit settings: Settings): Reader = {
    new BufferedReader(new InputStreamReader(new FileInputStream(file), settings.inputEncoding))
  }

  /**
   * Write the specified ScalaClass to a file, or to stdout, depending on the Settings.
   */
  private def write(generated: ScalaClass)(implicit settings: Settings): Unit = {
    if (settings.stdout) {
      println(generated)
    } else {

      val targetDir = new File(settings.outputDirectory + File.separator + generated.path)

      // generate all the directories between outputDirectory and generated.path
      // target directory exists because the passed option is checked in option()
      targetDir.mkdirs()

      val targetFile = new File(targetDir, generated.file.camelCase + ".scala")

      if (targetFile.exists()) targetFile.delete()

      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), settings.outputEncoding))

      try {
        writer.write(ScalaFormatter.format(generated.body,
          FormattingPreferences()
            .setPreference(AlignSingleLineCaseStatements, true)
            .setPreference(DoubleIndentClassDeclaration, true)
        ))
      } finally {
        writer.close()
      }
    }
  }

}
