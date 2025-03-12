package com.myapp.business

import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

trait WardrobeLogic[F[_]] {

  def wardrobeRegSecurityLogic(
    orgToken: OrgToken
  ): F[Either[ApiError, OrgId]]

  def wardrobeRegBusinessLogic(
    orgId: OrgId,
    req: WardrobeRegRequest
  ): F[Either[ApiError, WardrobeRegResponse]]

  def wardrobeInfoSecurityLogic(
    orgToken: OrgToken,
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeInfoResponse]]

  def wardrobeInfoBusinessLogic(
    res: WardrobeInfoResponse
  ): F[Either[ApiError, WardrobeInfoResponse]]

  def wardrobeDelSecurityLogic(
    orgToken: OrgToken,
    wardrobeToken: WardrobeToken
  ): F[Either[ApiError, WardrobeInfoResponse]]

  def wardrobeDelBusinessLogic(
    res: WardrobeInfoResponse
  ): F[Either[ApiError, WardrobeInfoResponse]]

}
