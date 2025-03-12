package com.myapp.database

import cats.effect.Resource
import doobie.hikari.HikariTransactor

trait CreateTransactorTrait[F[_]] {
  def createTransactor: Resource[F, HikariTransactor[F]]
}
