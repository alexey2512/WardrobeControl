package com.myapp.error

import cats.syntax.semigroup._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.EndpointOutput._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import tofu.logging._
import tofu.syntax.logRenderer.LogRendererTopContextOps

sealed trait ApiError extends Throwable {
  def message: String
}

object ApiError {
  case class InternalServerError(message: String)      extends ApiError
  case class BadRequestError(message: String)          extends ApiError
  case class NotFoundError(message: String)            extends ApiError
  case class UnauthorizedError(message: String)        extends ApiError
  case class ForbiddenError(message: String)           extends ApiError
  case class ServiceUnavailableError(message: String)  extends ApiError
  case class UnprocessableEntityError(message: String) extends ApiError

  implicit val apiErrorLoggable: Loggable[ApiError] =
    new DictLoggable[ApiError] {
      override def fields[I, V, R, S](a: ApiError, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        a match {
          case InternalServerError(msg) =>
            i.addString("errorType", "InternalServerError") |+|
              i.addString("message", msg)
          case BadRequestError(msg) =>
            i.addString("errorType", "BadRequestError") |+|
              i.addString("message", msg)
          case NotFoundError(msg) =>
            i.addString("errorType", "NotFoundError") |+|
              i.addString("message", msg)
          case UnauthorizedError(msg) =>
            i.addString("errorType", "UnauthorizedError") |+|
              i.addString("message", msg)
          case ForbiddenError(msg) =>
            i.addString("errorType", "ForbiddenError") |+|
              i.addString("message", msg)
          case ServiceUnavailableError(msg) =>
            i.addString("errorType", "ServiceUnavailable") |+|
              i.addString("message", msg)
          case UnprocessableEntityError(msg) =>
            i.addString("errorType", "UnprocessableEntityError") |+|
              i.addString("message", msg)
        }

      override def logShow(a: ApiError): String = a.toString
    }

  private val internalServerErrorV: OneOfVariant[InternalServerError] =
    oneOfVariant(
      StatusCode.InternalServerError,
      jsonBody[InternalServerError]
        .description("when server logic or database error occurred")
    )

  private val badRequestErrorV: OneOfVariant[BadRequestError] =
    oneOfVariant(
      StatusCode.BadRequest,
      jsonBody[BadRequestError]
        .description("when request body is unacceptable")
    )

  private val notFoundErrorV: OneOfVariant[NotFoundError] =
    oneOfVariant(
      StatusCode.NotFound,
      jsonBody[NotFoundError]
        .description("when some entity not found")
    )

  private val unauthorizedErrorV: OneOfVariant[UnauthorizedError] =
    oneOfVariant(
      StatusCode.Unauthorized,
      jsonBody[UnauthorizedError]
        .description("when token parsing failed or no such entity registered")
    )

  private val forbiddenErrorV: OneOfVariant[ForbiddenError] =
    oneOfVariant(
      StatusCode.Forbidden,
      jsonBody[ForbiddenError]
        .description("when token is parsed but its role inappropriate")
    )

  private val serviceUnavailableErrorV: OneOfVariant[ServiceUnavailableError] =
    oneOfVariant(
      StatusCode.ServiceUnavailable,
      jsonBody[ServiceUnavailableError]
        .description("when can not perform operation due to situation")
    )

  private val unprocessableEntityErrorV
    : OneOfVariant[UnprocessableEntityError] =
    oneOfVariant(
      StatusCode.UnprocessableEntity,
      jsonBody[UnprocessableEntityError]
        .description("when situation doesn't allow perform operation")
    )

  val registrationFailedVariants: OneOf[ApiError, ApiError] = oneOf[ApiError](
    internalServerErrorV,
    badRequestErrorV,
    unauthorizedErrorV,
    forbiddenErrorV
  )

  val operationFailedVariants: OneOf[ApiError, ApiError] = oneOf[ApiError](
    internalServerErrorV,
    notFoundErrorV,
    unauthorizedErrorV,
    forbiddenErrorV
  )

  val ableFailedVariants: OneOf[ApiError, ApiError] = oneOf[ApiError](
    internalServerErrorV,
    notFoundErrorV,
    badRequestErrorV,
    unauthorizedErrorV,
    forbiddenErrorV,
    unprocessableEntityErrorV
  )

  val actionFailedVariants: OneOf[ApiError, ApiError] = oneOf[ApiError](
    internalServerErrorV,
    notFoundErrorV,
    unauthorizedErrorV,
    forbiddenErrorV,
    serviceUnavailableErrorV
  )

}
