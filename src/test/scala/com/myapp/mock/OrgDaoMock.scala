package com.myapp.mock

import cats.data.EitherT
import cats.effect.{Async, Ref}
import cats.syntax.functor._
import com.myapp.database.OrgDao
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

import java.time.LocalDateTime

class OrgDaoMock[F[_]: Async](
  ref: Ref[F, Map[OrgId, Organization]],
  personDao: PersonDaoMock[F],
  wardrobeDao: WardrobeDaoMock[F]
) extends OrgDao[F] {

  override def insertOrg(
    name: WardrobeToken,
    address: WardrobeToken,
    token: OrgToken
  ): F[Either[DatabaseError, OrgRegInfo]] =
    ref.modify { state =>
      val nextId: OrgId          = state.keys.maxOption.getOrElse(-1) + 1
      val current: LocalDateTime = LocalDateTime.now()
      val result: Either[DatabaseError, OrgRegInfo] =
        state.values.find(_.token == token) match {
          case Some(_) => Left(UnknownErrorD("token conflict"))
          case None    => Right(OrgRegInfo(nextId, current))
        }
      (state + (nextId -> Organization(name, address, token, current)), result)
    }

  override def findOrg(token: OrgToken): F[Either[DatabaseError, OrgInfo]] =
    ref.get.map(
      _.find { case (_, org) => org.token == token } match {
        case Some((id, org)) =>
          Right(OrgInfo(id, org.name, org.address, org.registeredAt))
        case None => Left(NotFoundErrorD("no such org"))
      }
    )

  override def deleteOrg(id: OrgId): F[Either[DatabaseError, Unit]] =
    ref.flatModify { state =>
      val result: F[Either[DatabaseError, Unit]] =
        (for {
          _ <- EitherT.fromEither[F](
            state.keys.find(_ == id) match {
              case Some(_) => Right(())
              case None => Left(DeleteErrorD("failed to delete organization"))
            }
          )
          _  <- EitherT(personDao.deleteAllInOrg(id))
          r2 <- EitherT(wardrobeDao.deleteAllInOrg(id))
        } yield r2).value

      (state.removed(id), result)
    }

}
