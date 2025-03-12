package com.myapp.api

import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import sttp.tapir._
import sttp.tapir.json.circe._

private[api] object WardrobeEndpoints {

  val wardrobeRegEndpoint: Endpoint[
    OrgToken,
    WardrobeRegRequest,
    ApiError,
    WardrobeRegResponse,
    Any
  ] =
    endpoint.post
      .securityIn(header[OrgToken]("OrganizationToken"))
      .in("wardrobe" / "reg")
      .in(jsonBody[WardrobeRegRequest])
      .out(jsonBody[WardrobeRegResponse])
      .errorOut(registrationFailedVariants)

  val wardrobeInfoEndpoint: Endpoint[
    (OrgToken, WardrobeToken),
    Unit,
    ApiError,
    WardrobeInfoResponse,
    Any
  ] =
    endpoint.get
      .securityIn(header[OrgToken]("OrganizationToken"))
      .securityIn(header[WardrobeToken]("WardrobeToken"))
      .in("wardrobe" / "info")
      .out(jsonBody[WardrobeInfoResponse])
      .errorOut(operationFailedVariants)

  val wardrobeDelEndpoint: Endpoint[
    (OrgToken, WardrobeToken),
    Unit,
    ApiError,
    WardrobeInfoResponse,
    Any
  ] =
    endpoint.delete
      .securityIn(header[OrgToken]("OrganizationToken"))
      .securityIn(header[WardrobeToken]("WardrobeToken"))
      .in("wardrobe" / "del")
      .out(jsonBody[WardrobeInfoResponse])
      .errorOut(operationFailedVariants)

}
