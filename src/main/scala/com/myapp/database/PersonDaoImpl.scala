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
import PersonQueries._

class PersonDaoImpl[F[_]: Async](txr: Resource[F, HikariTransactor[F]])
    extends PersonDao[F] {

  override def insertPerson(
    orgId: OrgId,
    name: String,
    token: PersonToken
  ): F[Either[DatabaseError, PersonRegInfo]] =
    txr.use { xa =>
      EitherT(
        MonadThrow[F].attempt(insertPersonQ(orgId, name, token).transact(xa))
      )
        .leftMap(_ =>
          UnknownErrorD("failed to create new person"): DatabaseError
        )
        .value
    }

  override def findPerson(
    token: PersonToken
  ): F[Either[DatabaseError, PersonInfo]] =
    txr.use { xa =>
      MonadThrow[F].attempt(findPersonQ(token).transact(xa)).map {
        case Right(Some(res)) => Right(res)
        case Right(None)      => Left(NotFoundErrorD("no such person"))
        case Left(_)          => Left(UnknownErrorD("some sql error"))
      }
    }

  override def deletePerson(
    id: PersonId
  ): F[Either[DatabaseError, Unit]] =
    txr.use { xa =>
      MonadThrow[F].attempt(deletePersonQ(id).transact(xa)).map {
        case Right(1) => Right(())
        case Right(_) => Left(DeleteErrorD("failed to delete person"))
        case Left(_)  => Left(UnknownErrorD("some sql error"))
      }
    }

  override def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]] =
    txr.use { xa =>
      EitherT(MonadThrow[F].attempt(countInOrgQ(orgId).transact(xa)))
        .leftMap(_ =>
          UnknownErrorD(
            "failed to count persons in organization"
          ): DatabaseError
        )
        .value
    }

}
