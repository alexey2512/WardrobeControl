package com.myapp.api

import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.types.AuthTokenTypes._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

private[api] object HookEndpoints {

  val enableHookEndpoint
    : Endpoint[WardrobeToken, HookNumberRequest, ApiError, Unit, Any] =
    endpoint.patch
      .securityIn(header[WardrobeToken]("WardrobeToken"))
      .in("hook" / "enable")
      .in(jsonBody[HookNumberRequest])
      .out(statusCode(StatusCode.NoContent))
      .errorOut(ableFailedVariants)

  val disableHookEndpoint
    : Endpoint[WardrobeToken, HookNumberRequest, ApiError, Unit, Any] =
    endpoint.patch
      .securityIn(header[WardrobeToken]("WardrobeToken"))
      .in("hook" / "disable")
      .in(jsonBody[HookNumberRequest])
      .out(statusCode(StatusCode.NoContent))
      .errorOut(ableFailedVariants)

  val hookActionEndpoint: Endpoint[
    (WardrobeToken, PersonToken),
    HookActionRequest,
    ApiError,
    HookActionResponse,
    Any
  ] =
    endpoint.put
      .securityIn(header[WardrobeToken]("WardrobeToken"))
      .securityIn(header[PersonToken]("PersonToken"))
      .in("hook" / "action")
      .in(jsonBody[HookActionRequest])
      .out(jsonBody[HookActionResponse])
      .errorOut(actionFailedVariants)

}
