package com.myapp.database

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext

class DataAccessEnvironment {

  private val container = new TestContainer()

  def start(): Unit = container.make().start()

  def finish(): Unit = {
    container.stop()
    container.close()
  }

  private def transactor: Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor(
      TestContainer.DRIVER,
      container.getJdbcUrl,
      container.getUsername,
      container.getPassword,
      ExecutionContext.global
    )

  def makeOrgDao: OrgDao[IO]           = new OrgDaoImpl[IO](transactor)
  def makePersonDao: PersonDao[IO]     = new PersonDaoImpl[IO](transactor)
  def makeWardrobeDao: WardrobeDao[IO] = new WardrobeDaoImpl[IO](transactor)
  def makeHookDao: HookDao[IO]         = new HookDaoImpl[IO](transactor)
}
