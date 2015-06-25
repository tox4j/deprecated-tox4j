import sbt.tox4j._
import sbt.tox4j.lint._

object ThisBuild extends sbt.Build {

  lazy val root = Tox4jProject(
    "root",
    // Build plugins.
    Assembly,
    Benchmarking,
    Jni,
    MakeScripts,
    ProtobufJni,

    // Lint plugins.
    Checkstyle,
    Findbugs,
    Scalastyle,
    WartRemover,
    Xlint,

    // Local overrides for linters.
    WartRemoverOverrides
  )

}
