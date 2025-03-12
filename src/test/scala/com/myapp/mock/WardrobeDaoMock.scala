package com.myapp.mock

import cats.data.EitherT
import cats.effect.{Async, Ref}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.myapp.database.WardrobeDao
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

import java.time.LocalDateTime

class WardrobeDaoMock[F[_]: Async](
  ref: Ref[F, Map[WardrobeId, Wardrobe]],
  hookDao: HookDaoMock[F]
) extends WardrobeDao[F] {

  override def insertWardrobe(
    orgId: OrgId,
    name: WardrobeToken,
    hooksCount: OrgId,
    token: WardrobeToken
  ): F[Either[DatabaseError, WardrobeRegInfo]] =
    ref.modify { state =>
      val nextId: WardrobeId     = state.keys.maxOption.getOrElse(-1) + 1
      val current: LocalDateTime = LocalDateTime.now()
      val result: Either[DatabaseError, WardrobeRegInfo] =
        state.values.find(_.token == token) match {
          case Some(_) => Left(UnknownErrorD("token conflict"))
          case None    => Right(WardrobeRegInfo(nextId, current))
        }
      (
        state + (nextId -> Wardrobe(orgId, name, hooksCount, token, current)),
        result
      )
    }

  override def findWardrobe(
    token: WardrobeToken
  ): F[Either[DatabaseError, WardrobeInfo]] =
    ref.get.map(
      _.find { case (_, wardrobe) => wardrobe.token == token } match {
        case Some((id, wardrobe)) =>
          Right(
            WardrobeInfo(
              id,
              wardrobe.orgId,
              wardrobe.name,
              wardrobe.hooksCount,
              wardrobe.registeredAt
            )
          )
        case None => Left(NotFoundErrorD("no such wardrobe"))
      }
    )

  override def deleteWardrobe(id: WardrobeId): F[Either[DatabaseError, Unit]] =
    ref.flatModify { state =>
      val result: F[Either[DatabaseError, Unit]] =
        (for {
          _ <- EitherT.fromEither[F](
            state.keys.find(_ == id) match {
              case Some(_) => Right(())
              case None => Left(DeleteErrorD("failed to delete organization"))
            }
          )
          r <- EitherT(hookDao.deleteAllInWardrobe(id))
        } yield r).value

      (state.removed(id), result)
    }

  override def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]] =
    ref.get.map(state => Right(state.values.count(_.orgId == orgId)))

  def deleteAllInOrg(orgId: OrgId): F[Either[DatabaseError, Unit]] =
    ref.get.flatMap {
      _.collect {
        case (id, wardrobe) if wardrobe.orgId == orgId => id
      }.toList
        .map(deleteWardrobe)
        .foldLeft(
          (Right(()): Either[DatabaseError, Unit]).pure
        )((a, b) => a.flatMap(_ => b))
    }

}
