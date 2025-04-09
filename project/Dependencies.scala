import sbt.*

object Dependencies {

  val logback_classic = "ch.qos.logback" % "logback-classic" % "1.5.18"
  val postgres_driver = "org.postgresql" % "postgresql" % "42.7.5"
  val scalatest = "org.scalatest" %% "scalatest" % "3.2.19" % Test
  val blaze_server = "org.http4s" %% "http4s-blaze-server" % "0.23.17"
  val kind_projector = ("org.typelevel" %% "kind-projector" % "0.13.3").cross(CrossVersion.full)
  val jwt_scala = "com.github.jwt-scala" %% "jwt-core" % "10.0.4"

  object Cats {
    val libs: Seq[ModuleID] = Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0" % Test
    )
  }

  object Circe {
    val version: String = "0.14.12"
    val libs: Seq[ModuleID] = Seq(
      "io.circe" %% "circe-core" % version,
      "io.circe" %% "circe-generic" % version,
      "io.circe" %% "circe-parser" % version
    )
  }

  object Tapir {
    val version: String = "1.11.23"
    val libs: Seq[ModuleID] = Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % version,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % version,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % version,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % version,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % version
    )
  }

  object Doobie {
    val version: String = "1.0.0-RC8"
    val libs: Seq[ModuleID] = Seq(
      "org.tpolecat" %% "doobie-core" % version,
      "org.tpolecat" %% "doobie-postgres" % version,
      "org.tpolecat" %% "doobie-hikari" % version
    )
  }

  object Pureconfig {
    val version: String = "0.17.8"
    val libs: Seq[ModuleID] = Seq(
      "com.github.pureconfig" %% "pureconfig" % version,
      "com.github.pureconfig" %% "pureconfig-core" % version,
      "com.github.pureconfig" %% "pureconfig-generic" % version,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % version
    )
  }

  object Tofu {
    val version: String = "0.13.7"
    val libs: Seq[ModuleID] = Seq(
      "tf.tofu" %% "tofu-kernel" % version,
      "tf.tofu" %% "tofu-core-ce3" % version,
      "tf.tofu" %% "tofu-logging" % version,
      "tf.tofu" %% "tofu-logging-derivation" % version
    )
  }

  object Slf4j {
    val version: String = "2.0.17"
    val libs: Seq[ModuleID] = Seq(
      "org.slf4j" % "slf4j-api" % version,
      "org.slf4j" % "jul-to-slf4j" % version
    )
  }

}