package com.myapp.api

import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import sttp.tapir._
import sttp.tapir.json.circe._

private[api] object PersonEndpoints {

  val personRegEndpoint
    : Endpoint[OrgToken, PersonRegRequest, ApiError, PersonRegResponse, Any] =
    endpoint.post
      .securityIn(header[OrgToken]("OrganizationToken"))
      .in("person" / "reg")
      .in(jsonBody[PersonRegRequest])
      .out(jsonBody[PersonRegResponse])
      .errorOut(registrationFailedVariants)

  val personInfoEndpoint: Endpoint[
    (OrgToken, PersonToken),
    Unit,
    ApiError,
    PersonInfoResponse,
    Any
  ] =
    endpoint.get
      .securityIn(header[OrgToken]("OrganizationToken"))
      .securityIn(header[PersonToken]("PersonToken"))
      .in("person" / "info")
      .out(jsonBody[PersonInfoResponse])
      .errorOut(operationFailedVariants)

  val personDelEndpoint: Endpoint[
    (OrgToken, PersonToken),
    Unit,
    ApiError,
    PersonInfoResponse,
    Any
  ] =
    endpoint.delete
      .securityIn(header[OrgToken]("OrganizationToken"))
      .securityIn(header[PersonToken]("PersonToken"))
      .in("person" / "del")
      .out(jsonBody[PersonInfoResponse])
      .errorOut(operationFailedVariants)

}
