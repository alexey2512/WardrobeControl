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
import WardrobeQueries._

class WardrobeDaoImpl[F[_]: Async](txr: Resource[F, HikariTransactor[F]])
    extends WardrobeDao[F] {

  override def insertWardrobe(
    orgId: OrgId,
    name: WardrobeToken,
    hooksCount: OrgId,
    token: WardrobeToken
  ): F[Either[DatabaseError, WardrobeRegInfo]] =
    txr.use { xa =>
      EitherT(
        MonadThrow[F].attempt(
          insertWardrobeQ(orgId, name, hooksCount, token).transact(xa)
        )
      )
        .leftMap(_ =>
          UnknownErrorD("failed to create new wardrobe"): DatabaseError
        )
        .value
    }

  override def findWardrobe(
    token: WardrobeToken
  ): F[Either[DatabaseError, WardrobeInfo]] =
    txr.use { xa =>
      MonadThrow[F].attempt(findWardrobeQ(token).transact(xa)).map {
        case Right(Some(res)) => Right(res)
        case Right(None)      => Left(NotFoundErrorD("no such wardrobe"))
        case Left(_)          => Left(UnknownErrorD("some sql error"))
      }
    }

  override def deleteWardrobe(
    id: WardrobeId
  ): F[Either[DatabaseError, Unit]] =
    txr.use { xa =>
      MonadThrow[F].attempt(deleteWardrobeQ(id).transact(xa)).map {
        case Right(1) => Right(())
        case Right(_) => Left(DeleteErrorD("failed to delete wardrobe"))
        case Left(_)  => Left(UnknownErrorD("some sql error"))
      }
    }

  override def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]] =
    txr.use { xa =>
      EitherT(MonadThrow[F].attempt(countInOrgQ(orgId).transact(xa)))
        .leftMap(_ =>
          UnknownErrorD(
            "failed to count wardrobes in organization"
          ): DatabaseError
        )
        .value
    }

}
