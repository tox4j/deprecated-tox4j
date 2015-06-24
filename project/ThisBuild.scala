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
    ProtobufPlugin,

    // Lint plugins.
    Checkstyle,
    Findbugs,
    Scalastyle,
    Scapegoat,
    WartRemover,
    Xlint,

    // Local overrides for linters.
    WartRemoverOverrides
  ).configs(ProtobufPlugin.Protobuf)

}
