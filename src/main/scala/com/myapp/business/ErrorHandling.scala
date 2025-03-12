package com.myapp.business

import com.myapp.error._
import ApiError._
import DatabaseError._

private[business] object ErrorHandling {

  val logicErrorMapping: DatabaseError => ApiError = {
    case NotFoundErrorD(msg)            => NotFoundError(msg)
    case UnprocessableEntityErrorD(msg) => UnprocessableEntityError(msg)
    case err                            => InternalServerError(err.msg)
  }

  val authErrorMapping: DatabaseError => ApiError = {
    case NotFoundErrorD(msg) => UnauthorizedError(msg)
    case err                 => InternalServerError(err.msg)
  }

  def checkNonEmpty(name: String): Either[ApiError, Unit] = name match {
    case "" => Left(BadRequestError("name must be non empty"))
    case _  => Right(())
  }

  def checkPositive(number: Int, param: String): Either[ApiError, Unit] =
    number match {
      case n if n <= 0 =>
        Left(BadRequestError(s"$param must be positive integer"))
      case _ => Right(())
    }

}
