package com.myapp.error

sealed trait DatabaseError extends Throwable {
  def msg: String
}

object DatabaseError {
  case class UnknownErrorD(msg: String)             extends DatabaseError
  case class NotFoundErrorD(msg: String)            extends DatabaseError
  case class DeleteErrorD(msg: String)              extends DatabaseError
  case class PartialExecutionErrorD(msg: String)    extends DatabaseError
  case class UnprocessableEntityErrorD(msg: String) extends DatabaseError
}
