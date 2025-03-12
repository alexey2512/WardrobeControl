package com.myapp.business

import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._

trait OrgLogic[F[_]] {

  def orgRegSecurityLogic(
    ownerToken: OwnerToken
  ): F[Either[ApiError, Unit]]

  def orgRegBusinessLogic(
    req: OrgRegRequest
  ): F[Either[ApiError, OrgRegResponse]]

  def orgInfoSecurityLogic(
    ownerToken: OwnerToken,
    orgToken: OrgToken
  ): F[Either[ApiError, OrgInfoResponse]]

  def orgInfoBusinessLogic(
    res: OrgInfoResponse
  ): F[Either[ApiError, OrgInfoResponse]]

  def orgDelSecurityLogic(
    ownerToken: OwnerToken,
    orgToken: OrgToken
  ): F[Either[ApiError, OrgInfoResponse]]

  def orgDelBusinessLogic(
    res: OrgInfoResponse
  ): F[Either[ApiError, OrgInfoResponse]]

}
