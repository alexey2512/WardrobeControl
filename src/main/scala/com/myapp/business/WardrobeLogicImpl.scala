package com.myapp.business

import cats.Monad
import cats.data.EitherT
import com.myapp.database._
import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import ErrorHandling._

class WardrobeLogicImpl[F[_]: Monad](
  tokens: Tokens[F],
  orgDao: OrgDao[F],
  wardrobeDao: WardrobeDao[F],
  hookDao: HookDao[F]
) extends WardrobeLogic[F] {

  override def wardrobeRegSecurityLogic(
    orgToken: OrgToken
  ): F[Either[ApiError, OrgId]] =
    (
      for {
        _    <- EitherT.fromEither[F](tokens.checkOrg(orgToken))
        info <- EitherT(orgDao.findOrg(orgToken)).leftMap(authErrorMapping)
      } yield info.id
    ).value

  override def wardrobeRegBusinessLogic(
    orgId: OrgId,
    req: WardrobeRegRequest
  ): F[Either[ApiError, WardrobeRegResponse]] =
    (
      for {
        _ <- EitherT.fromEither[F](checkNonEmpty(req.name))
        _ <- EitherT.fromEither[F](checkPositive(req.hooksCount, "hooksCount"))
        token <- EitherT.liftF(tokens.generateWardrobeToken(req.name))
        info <- EitherT(
          wardrobeDao.insertWardrobe(orgId, req.name, req.hooksCount, token)
        ).leftMap(logicErrorMapping)
        _ <- EitherT(hookDao.insertHooks(info.id, req.hooksCount))
          .leftMap(logicErrorMapping)
      } yield WardrobeRegResponse(info.id, token, info.registeredAt)
    ).value

  override def wardrobeInfoSecurityLogic(
    orgToken: OrgToken,
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeInfoResponse]] =
    (
      for {
        _ <- EitherT.fromEither[F](tokens.checkOrg(orgToken))
        _ <- EitherT.fromEither[F](tokens.checkWardrobe(wardrobeToken))
        _ <- EitherT(orgDao.findOrg(orgToken)).leftMap(authErrorMapping)
        info <- EitherT(wardrobeDao.findWardrobe(wardrobeToken))
          .leftMap(authErrorMapping)
      } yield WardrobeInfoResponse(
        info.id,
        info.name,
        info.hooksCount,
        info.registeredAt
      )
    ).value

  private def fillOtherFields(
    res: WardrobeInfoResponse
  ): EitherT[F, ApiError, WardrobeInfoResponse] =
    for {
      enabled <- EitherT(hookDao.countAllEnabledHooks(res.id))
        .leftMap(logicErrorMapping)
      enabledFree <- EitherT(hookDao.countAllEnabledFreeHooks(res.id))
        .leftMap(logicErrorMapping)
    } yield res.copy(
      enabledHooksCount = enabled,
      enabledFreeHooksCount = enabledFree
    )

  override def wardrobeInfoBusinessLogic(
    res: WardrobeInfoResponse
  ): F[Either[ApiError, WardrobeInfoResponse]] =
    fillOtherFields(res).value

  override def wardrobeDelSecurityLogic(
    orgToken: OrgToken,
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeInfoResponse]] =
    wardrobeInfoSecurityLogic(orgToken, wardrobeToken)

  override def wardrobeDelBusinessLogic(
    res: WardrobeInfoResponse
  ): F[Either[ApiError, WardrobeInfoResponse]] =
    (
      for {
        info <- fillOtherFields(res)
        _ <- EitherT(wardrobeDao.deleteWardrobe(res.id))
          .leftMap(logicErrorMapping)
      } yield info
    ).value

}
