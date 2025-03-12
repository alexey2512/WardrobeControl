package com.myapp.api

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroup._
import tofu.logging._

class LogHelpers[F[_]: Monad: Logging] {

  implicit def tuple2Loggable[A: Loggable, B: Loggable]: Loggable[(A, B)] =
    new DictLoggable[(A, B)] {
      override def fields[I, V, R, S](a: (A, B), i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        Loggable[A].fields(a._1, i) |+|
          Loggable[B].fields(a._2, i)

      def logShow(a: (A, B)): String =
        s"(${Loggable[A].logShow(a._1)}, ${Loggable[B].logShow(a._2)})"
    }

  implicit def tuple3Loggable[A: Loggable, B: Loggable, C: Loggable]
    : Loggable[(A, B, C)] =
    new DictLoggable[(A, B, C)] {
      override def fields[I, V, R, S](a: (A, B, C), i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        Loggable[A].fields(a._1, i) |+|
          Loggable[B].fields(a._2, i) |+|
          Loggable[C].fields(a._3, i)

      override def logShow(a: (A, B, C)): String =
        s"(${Loggable[A].logShow(a._1)}, ${Loggable[B].logShow(a._2)}, ${Loggable[C].logShow(a._3)})"
    }

  def logWrapper[I: Loggable, E: Loggable, O: Loggable](
    f: I => F[Either[E, O]],
    route: String
  ): I => F[Either[E, O]] = input =>
    for {
      _ <- Logging[F].info(
        s"$route: received input body",
        Map("input" -> input)
      )
      either <- f(input)
      _ <- either match {
        case Left(error) =>
          Logging[F].warn(
            s"$route: returning error out",
            Map("error" -> error)
          )
        case Right(output) =>
          Logging[F].info(
            s"$route: returning successful out",
            Map("output" -> output)
          )
      }
    } yield either

}

object LogHelpers {
  def apply[F[_]: Monad: Logging]: LogHelpers[F] = new LogHelpers[F]
}
