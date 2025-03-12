package com.myapp.api

import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import sttp.tapir._
import sttp.tapir.json.circe._

private[api] object OrgEndpoints {

  val orgRegEndpoint
    : Endpoint[OwnerToken, OrgRegRequest, ApiError, OrgRegResponse, Any] =
    endpoint.post
      .securityIn(header[OwnerToken]("ProjectOwnerToken"))
      .in("org" / "reg")
      .in(jsonBody[OrgRegRequest])
      .out(jsonBody[OrgRegResponse])
      .errorOut(registrationFailedVariants)

  val orgInfoEndpoint
    : Endpoint[(OwnerToken, OrgToken), Unit, ApiError, OrgInfoResponse, Any] =
    endpoint.get
      .securityIn(header[OwnerToken]("ProjectOwnerToken"))
      .securityIn(header[OrgToken]("OrganizationToken"))
      .in("org" / "info")
      .out(jsonBody[OrgInfoResponse])
      .errorOut(operationFailedVariants)

  val orgDelEndpoint
    : Endpoint[(OwnerToken, OrgToken), Unit, ApiError, OrgInfoResponse, Any] =
    endpoint.delete
      .securityIn(header[OwnerToken]("ProjectOwnerToken"))
      .securityIn(header[OrgToken]("OrganizationToken"))
      .in("org" / "del")
      .out(jsonBody[OrgInfoResponse])
      .errorOut(operationFailedVariants)

}
