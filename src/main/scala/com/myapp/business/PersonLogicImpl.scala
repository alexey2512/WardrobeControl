package com.myapp.business

import cats.Monad
import cats.data.EitherT
import cats.syntax.applicative._
import com.myapp.database._
import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import ErrorHandling._

class PersonLogicImpl[F[_]: Monad](
  tokens: Tokens[F],
  orgDao: OrgDao[F],
  personDao: PersonDao[F]
) extends PersonLogic[F] {

  override def personRegSecurityLogic(
    orgToken: OrgToken
  ): F[Either[ApiError, OrgId]] =
    (
      for {
        _    <- EitherT.fromEither[F](tokens.checkOrg(orgToken))
        info <- EitherT(orgDao.findOrg(orgToken)).leftMap(authErrorMapping)
      } yield info.id
    ).value

  override def personRegBusinessLogic(
    orgId: OrgId,
    req: PersonRegRequest
  ): F[Either[ApiError, PersonRegResponse]] =
    (
      for {
        _     <- EitherT.fromEither[F](checkNonEmpty(req.name))
        token <- EitherT.liftF(tokens.generatePersonToken(req.name))
        info <- EitherT(personDao.insertPerson(orgId, req.name, token))
          .leftMap(logicErrorMapping)
      } yield PersonRegResponse(info.id, token, info.registeredAt)
    ).value

  override def personInfoSecurityLogic(
    orgToken: OrgToken,
    personToken: PersonToken
  ): F[Either[ApiError, PersonInfoResponse]] =
    (
      for {
        _ <- EitherT.fromEither[F](tokens.checkOrg(orgToken))
        _ <- EitherT.fromEither[F](tokens.checkPerson(personToken))
        _ <- EitherT(orgDao.findOrg(orgToken)).leftMap(authErrorMapping)
        info <- EitherT(personDao.findPerson(personToken))
          .leftMap(authErrorMapping)
      } yield PersonInfoResponse(info.id, info.name, info.registeredAt)
    ).value

  override def personInfoBusinessLogic(
    res: PersonInfoResponse
  ): F[Either[ApiError, PersonInfoResponse]] =
    (Right(res): Either[ApiError, PersonInfoResponse]).pure[F]

  override def personDelSecurityLogic(
    orgToken: OrgToken,
    personToken: PersonToken
  ): F[Either[ApiError, PersonInfoResponse]] =
    personInfoSecurityLogic(orgToken, personToken)

  override def personDelBusinessLogic(
    res: PersonInfoResponse
  ): F[Either[ApiError, PersonInfoResponse]] =
    EitherT(personDao.deletePerson(res.id))
      .leftMap(logicErrorMapping)
      .map(_ => res)
      .value

}
