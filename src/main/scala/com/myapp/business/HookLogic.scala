package com.myapp.business

import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.IdTypes._
import com.myapp.types.AuthTokenTypes._

trait HookLogic[F[_]] {

  def enableHookSecurityLogic(
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeId]]

  def enableHookBusinessLogic(
    wardrobeId: WardrobeId,
    req: HookNumberRequest
  ): F[Either[ApiError, Unit]]

  def disableHookSecurityLogic(
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeId]]

  def disableHookBusinessLogic(
    wardrobeId: WardrobeId,
    req: HookNumberRequest
  ): F[Either[ApiError, Unit]]

  def hookActionSecurityLogic(
    wardrobeToken: WardrobeToken,
    personToken: PersonToken
  ): F[Either[ApiError, (WardrobeId, PersonId)]]

  def hookActionBusinessLogic(
    wardrobeId: WardrobeId,
    personId: PersonId,
    req: HookActionRequest
  ): F[Either[ApiError, HookActionResponse]]

}
