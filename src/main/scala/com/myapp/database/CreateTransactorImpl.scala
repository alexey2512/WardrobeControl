package com.myapp.database

import cats.effect.{Async, Resource}
import cats.syntax.functor._
import com.myapp.config.AppConfig
import doobie.hikari.HikariTransactor
import pureconfig._
import pureconfig.module.catseffect.syntax._
import doobie.util.log.LogHandler

import scala.concurrent.ExecutionContext

class CreateTransactorImpl[F[_]: Async] extends CreateTransactorTrait[F] {

  override def createTransactor: Resource[F, HikariTransactor[F]] =
    for {
      config <- Resource.eval(
        ConfigSource.default.loadF[F, AppConfig]().map(_.database)
      )
      transactor <- HikariTransactor.newHikariTransactor[F](
        config.driver,
        config.url,
        config.user,
        config.password,
        ExecutionContext.global,
        Some(LogHandler.jdkLogHandler)
      )
    } yield transactor

}
