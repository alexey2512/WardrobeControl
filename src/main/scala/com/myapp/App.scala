package com.myapp

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.myapp.api._
import com.myapp.business._
import com.myapp.config.AppConfig
import com.myapp.database._
import doobie.hikari.HikariTransactor
import org.http4s.blaze.server.BlazeServerBuilder
import org.slf4j.bridge.SLF4JBridgeHandler
import pureconfig._
import pureconfig.module.catseffect.syntax._
import tofu.logging._
import java.util.logging.LogManager

object App extends IOApp {

  private def fixLoggers(): IO[Unit] = IO {
    LogManager.getLogManager.reset()
    SLF4JBridgeHandler.install()
  }

  private def startServer(router: Router[IO]): IO[ExitCode] = {
    implicit val appLogging: Logging[IO] =
      Logging.Make.plain[IO].byName("app.log").asLogging
    for {
      _      <- fixLoggers()
      config <- ConfigSource.default.loadF[IO, AppConfig]().map(_.server)
      routes <- router.httpRoutes
      _ <- Resource
        .make(
          appLogging.info(
            s"application started, listening https://${config.host}:${config.port}"
          )
        )(_ => appLogging.info("application closed"))
        .flatMap(_ =>
          Resource.eval(
            BlazeServerBuilder[IO]
              .bindHttp(config.port, config.host)
              .withHttpApp(routes.orNotFound)
              .serve
              .compile
              .drain
          )
        )
        .use(_ => IO.never)
    } yield ExitCode.Success
  }

  private def initAndStart(): IO[ExitCode] = {
    val txr: Resource[IO, HikariTransactor[IO]] =
      new CreateTransactorImpl[IO].createTransactor

    val orgDao: OrgDao[IO]           = new OrgDaoImpl[IO](txr)
    val personDao: PersonDao[IO]     = new PersonDaoImpl[IO](txr)
    val wardrobeDao: WardrobeDao[IO] = new WardrobeDaoImpl[IO](txr)
    val hookDao: HookDao[IO]         = new HookDaoImpl[IO](txr)

    val tokens: Tokens[IO] = Tokens[IO]
    val orgLogic: OrgLogic[IO] =
      new OrgLogicImpl[IO](tokens, orgDao, personDao, wardrobeDao)
    val personLogic: PersonLogic[IO] =
      new PersonLogicImpl[IO](tokens, orgDao, personDao)
    val wardrobeLogic: WardrobeLogic[IO] =
      new WardrobeLogicImpl[IO](tokens, orgDao, wardrobeDao, hookDao)
    val hookLogic: HookLogic[IO] =
      new HookLogicImpl[IO](tokens, personDao, wardrobeDao, hookDao)

    implicit val apiLogging: Logging[IO] =
      Logging.Make.plain[IO].byName("api.log").asLogging
    val logHelpers: LogHelpers[IO] = LogHelpers[IO]
    val router: Router[IO] =
      new Router[IO](
        orgLogic,
        personLogic,
        wardrobeLogic,
        hookLogic,
        logHelpers
      )

    startServer(router)
  }

  private def printOwnerToken(name: String): IO[ExitCode] = {
    val tokens: Tokens[IO] = Tokens[IO]
    for {
      token <- tokens.generateOwnerToken(name)
      _     <- IO.println(token)
    } yield ExitCode.Success
  }

  override def run(args: List[String]): IO[ExitCode] = args match {
    case Nil         => initAndStart()
    case name :: Nil => printOwnerToken(name)
    case _ =>
      IO.println(
        "usage:\n" +
          "sbt run - starting server\n" +
          "sbt run <name> - generating owner token"
      ).as(ExitCode.Error)
  }

}
