package com.myapp.business

import com.myapp.error.ApiError
import com.myapp.model.api._
import com.myapp.types.IdTypes._
import com.myapp.types.AuthTokenTypes._

trait PersonLogic[F[_]] {

  def personRegSecurityLogic(
    orgToken: OrgToken
  ): F[Either[ApiError, OrgId]]

  def personRegBusinessLogic(
    orgId: OrgId,
    req: PersonRegRequest
  ): F[Either[ApiError, PersonRegResponse]]

  def personInfoSecurityLogic(
    orgToken: OrgToken,
    personToken: PersonToken
  ): F[Either[ApiError, PersonInfoResponse]]

  def personInfoBusinessLogic(
    res: PersonInfoResponse
  ): F[Either[ApiError, PersonInfoResponse]]

  def personDelSecurityLogic(
    orgToken: OrgToken,
    personToken: PersonToken
  ): F[Either[ApiError, PersonInfoResponse]]

  def personDelBusinessLogic(
    res: PersonInfoResponse
  ): F[Either[ApiError, PersonInfoResponse]]

}
