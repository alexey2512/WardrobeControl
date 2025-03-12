package com.myapp.business

import cats.Monad
import cats.data.EitherT
import cats.syntax.applicative._
import cats.syntax.flatMap._
import com.myapp.database._
import com.myapp.error._
import com.myapp.error.ApiError._
import com.myapp.error.DatabaseError._
import com.myapp.model.api._
import com.myapp.model.database._
import com.myapp.types.IdTypes._
import com.myapp.types.AuthTokenTypes._
import ErrorHandling._

class HookLogicImpl[F[_]: Monad](
  tokens: Tokens[F],
  personDao: PersonDao[F],
  wardrobeDao: WardrobeDao[F],
  hookDao: HookDao[F]
) extends HookLogic[F] {

  private def ableHookSecurityLogic(
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeId]] =
    (
      for {
        _ <- EitherT.fromEither[F](tokens.checkWardrobe(wardrobeToken))
        info <- EitherT(wardrobeDao.findWardrobe(wardrobeToken))
          .leftMap(authErrorMapping)
      } yield info.id
    ).value

  private def ableHookBusinessLogic(
    wardrobeId: WardrobeId,
    req: HookNumberRequest,
    action: HookId => F[Either[DatabaseError, Unit]]
  ): F[Either[ApiError, Unit]] =
    (
      for {
        _ <- EitherT.fromEither[F](checkPositive(req.number, "number"))
        hookId <- EitherT(hookDao.findHookByNumber(wardrobeId, req.number))
          .leftMap(logicErrorMapping)
        _ <- EitherT(action(hookId)).leftMap(logicErrorMapping)
      } yield ()
    ).value

  override def enableHookSecurityLogic(
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeId]] =
    ableHookSecurityLogic(wardrobeToken)

  override def enableHookBusinessLogic(
    wardrobeId: WardrobeId,
    req: HookNumberRequest
  ): F[Either[ApiError, Unit]] =
    ableHookBusinessLogic(wardrobeId, req, hookDao.enableHook)

  override def disableHookSecurityLogic(
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeId]] =
    ableHookSecurityLogic(wardrobeToken)

  override def disableHookBusinessLogic(
    wardrobeId: WardrobeId,
    req: HookNumberRequest
  ): F[Either[ApiError, Unit]] =
    ableHookBusinessLogic(wardrobeId, req, hookDao.disableHook)

  override def hookActionSecurityLogic(
    wardrobeToken: WardrobeToken,
    personToken: PersonToken
  ): F[Either[ApiError, (WardrobeId, PersonId)]] =
    (
      for {
        _ <- EitherT.fromEither[F](tokens.checkWardrobe(wardrobeToken))
        _ <- EitherT.fromEither[F](tokens.checkPerson(personToken))
        wardrobeInfo <- EitherT(wardrobeDao.findWardrobe(wardrobeToken))
          .leftMap(authErrorMapping)
        personInfo <- EitherT(personDao.findPerson(personToken))
          .leftMap(authErrorMapping)
      } yield (wardrobeInfo.id, personInfo.id)
    ).value

  override def hookActionBusinessLogic(
    wardrobeId: WardrobeId,
    personId: PersonId,
    req: HookActionRequest
  ): F[Either[ApiError, HookActionResponse]] =
    hookDao.findHookAssociatedWithPerson(wardrobeId, personId).flatMap {
      case Right(info) =>
        EitherT(hookDao.dropHook(info.id))
          .leftMap(logicErrorMapping)
          .map(_ => HookActionResponse(info.number, toTakeHook = false))
          .value
      case Left(NotFoundErrorD(_)) =>
        hookDao.findAllEnabledFreeHooks(wardrobeId).flatMap {
          case Right(Nil) =>
            (Left(ServiceUnavailableError("no free enabled hooks")): Either[
              ApiError,
              HookActionResponse
            ]).pure[F]
          case Right(hooks) =>
            val closest: HookInfo =
              hooks.minBy(info => Math.abs(info.number - req.currentPosition))
            EitherT(hookDao.takeHook(closest.id, personId))
              .leftMap(logicErrorMapping)
              .map(_ => HookActionResponse(closest.number, toTakeHook = true))
              .value
          case Left(e) =>
            (
              Left(logicErrorMapping(e)): Either[ApiError, HookActionResponse]
            ).pure[F]
        }
      case Left(e) =>
        (
          Left(logicErrorMapping(e)): Either[ApiError, HookActionResponse]
        ).pure[F]
    }

}
