package com.myapp.business

import ErrorHandling._
import cats.Monad
import cats.data.EitherT
import cats.syntax.applicative._
import com.myapp.database._
import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._

class OrgLogicImpl[F[_]: Monad](
  tokens: Tokens[F],
  orgDao: OrgDao[F],
  personDao: PersonDao[F],
  wardrobeDao: WardrobeDao[F]
) extends OrgLogic[F] {

  override def orgRegSecurityLogic(
    ownerToken: OwnerToken
  ): F[Either[ApiError, Unit]] =
    tokens.checkOwner(ownerToken).pure[F]

  override def orgRegBusinessLogic(
    req: OrgRegRequest
  ): F[Either[ApiError, OrgRegResponse]] =
    (
      for {
        _     <- EitherT.fromEither[F](checkNonEmpty(req.name))
        token <- EitherT.liftF(tokens.generateOrgToken(req.name))
        info <- EitherT(orgDao.insertOrg(req.name, req.address, token))
          .leftMap(logicErrorMapping)
      } yield OrgRegResponse(info.id, token, info.registeredAt)
    ).value

  override def orgInfoSecurityLogic(
    ownerToken: OwnerToken,
    orgToken: OrgToken
  ): F[Either[ApiError, OrgInfoResponse]] =
    (
      for {
        _    <- EitherT.fromEither[F](tokens.checkOwner(ownerToken))
        _    <- EitherT.fromEither[F](tokens.checkOrg(orgToken))
        info <- EitherT(orgDao.findOrg(orgToken)).leftMap(authErrorMapping)
      } yield OrgInfoResponse(
        info.id,
        info.name,
        info.address,
        info.registeredAt
      )
    ).value

  private def fillOtherFields(
    res: OrgInfoResponse
  ): EitherT[F, ApiError, OrgInfoResponse] =
    for {
      persons <- EitherT(personDao.countInOrg(res.id))
        .leftMap(logicErrorMapping)
      wardrobes <- EitherT(wardrobeDao.countInOrg(res.id))
        .leftMap(logicErrorMapping)
    } yield res.copy(persons = persons, wardrobes = wardrobes)

  override def orgInfoBusinessLogic(
    res: OrgInfoResponse
  ): F[Either[ApiError, OrgInfoResponse]] =
    fillOtherFields(res).value

  override def orgDelSecurityLogic(
    ownerToken: OwnerToken,
    orgToken: OrgToken
  ): F[Either[ApiError, OrgInfoResponse]] =
    orgInfoSecurityLogic(ownerToken, orgToken)

  override def orgDelBusinessLogic(
    res: OrgInfoResponse
  ): F[Either[ApiError, OrgInfoResponse]] =
    (
      for {
        info <- fillOtherFields(res)
        _    <- EitherT(orgDao.deleteOrg(res.id)).leftMap(logicErrorMapping)
      } yield info
    ).value

}
