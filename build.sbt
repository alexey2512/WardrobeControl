import Dependencies.*

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "WardrobeControl",
    version := "1.0.0"
  )

Compile / run / fork := true

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ymacro-annotations",
  "-Werror",
  "-Wextra-implicit",
  "-Wunused",
  "-Wvalue-discard",
  "-Xlint",
  "-Xlint:-byname-implicit",
  "-Xlint:-implicit-recursion"
)

libraryDependencies ++=
    Cats.libs ++
    Circe.libs ++
    Tapir.libs ++
    Doobie.libs ++
    Pureconfig.libs ++
    Tofu.libs ++
    Slf4j.libs ++
    Seq(
      logback_classic,
      postgres_driver,
      scalatest,
      blaze_server,
      kind_projector,
      jwt_scala
    )

addCompilerPlugin(kind_projector)
