package com.myapp.database

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.{Async, Resource}
import cats.syntax.functor._
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import OrgQueries._

class OrgDaoImpl[F[_]: Async](txr: Resource[F, HikariTransactor[F]])
    extends OrgDao[F] {

  override def insertOrg(
    name: String,
    address: String,
    token: OrgToken
  ): F[Either[DatabaseError, OrgRegInfo]] =
    txr.use { xa =>
      EitherT(
        MonadThrow[F].attempt(insertOrgQ(name, address, token).transact(xa))
      )
        .leftMap(_ =>
          UnknownErrorD("failed to create new organization"): DatabaseError
        )
        .value
    }

  override def findOrg(token: OrgToken): F[Either[DatabaseError, OrgInfo]] =
    txr.use { xa =>
      MonadThrow[F].attempt(findOrgQ(token).transact(xa)).map {
        case Right(Some(res)) => Right(res)
        case Right(None)      => Left(NotFoundErrorD("no such organization"))
        case Left(_)          => Left(UnknownErrorD("some sql error"))
      }
    }

  override def deleteOrg(id: OrgId): F[Either[DatabaseError, Unit]] =
    txr.use { xa =>
      MonadThrow[F].attempt(deleteOrgQ(id).transact(xa)).map {
        case Right(1) => Right(())
        case Right(_) => Left(DeleteErrorD("failed to delete organization"))
        case Left(_)  => Left(UnknownErrorD("some sql error"))
      }
    }

}
