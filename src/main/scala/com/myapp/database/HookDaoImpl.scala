package com.myapp.database

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.{Async, Resource}
import cats.syntax.functor._
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.IdTypes._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import HookQueries._

class HookDaoImpl[F[_]: Async](txr: Resource[F, HikariTransactor[F]])
    extends HookDao[F] {

  private def fromOptionQuery[P](
    io: ConnectionIO[Option[P]]
  ): F[Either[DatabaseError, P]] =
    txr.use { xa =>
      MonadThrow[F].attempt(io.transact(xa)).map {
        case Right(Some(res)) => Right(res)
        case Right(None)      => Left(NotFoundErrorD("no such hook"))
        case Left(_)          => Left(UnknownErrorD("some sql error"))
      }
    }

  private def withSimpleHandling[P](
    io: ConnectionIO[P]
  ): F[Either[DatabaseError, P]] =
    txr.use { xa =>
      EitherT(MonadThrow[F].attempt(io.transact(xa)))
        .leftMap(_ => UnknownErrorD("some sql error"): DatabaseError)
        .value
    }

  private def withUnitResult(
    io: ConnectionIO[Int]
  ): F[Either[DatabaseError, Unit]] =
    txr.use { xa =>
      MonadThrow[F].attempt(io.transact(xa)).map {
        case Right(c) if c == 1 => Right(())
        case Right(_) =>
          Left(UnprocessableEntityErrorD("unable to perform operation"))
        case Left(_) => Left(UnknownErrorD("some sql error"))
      }
    }

  override def insertHooks(
    wrId: WardrobeId,
    count: Int
  ): F[Either[DatabaseError, Unit]] =
    txr.use { xa =>
      MonadThrow[F].attempt(insertHooksQ(wrId, count).transact(xa)).map {
        case Right(c) if c == count => Right(())
        case Right(c) =>
          Left(PartialExecutionErrorD(s"created only $c/$count hooks"))
        case Left(_) => Left(UnknownErrorD("some sql error"))
      }
    }

  override def findHookByNumber(
    wrId: WardrobeId,
    number: Int
  ): F[Either[DatabaseError, HookId]] =
    fromOptionQuery[HookId](findHookByNumberQ(wrId, number))

  override def findHookAssociatedWithPerson(
    wrId: WardrobeId,
    occupiedBy: PersonId
  ): F[Either[DatabaseError, HookInfo]] =
    fromOptionQuery[HookInfo](findHookAssociatedWithPersonQ(wrId, occupiedBy))

  override def findAllEnabledFreeHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, List[HookInfo]]] =
    withSimpleHandling[List[HookInfo]](findAllEnabledFreeHooksQ(wrId))

  override def countAllEnabledHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, Int]] =
    withSimpleHandling[Int](countAllEnabledHooksQ(wrId))

  override def countAllEnabledFreeHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, Int]] =
    withSimpleHandling[Int](countAllEnabledFreeHooksQ(wrId))

  override def disableHook(id: HookId): F[Either[DatabaseError, Unit]] =
    withUnitResult(disableHookQ(id))

  override def enableHook(id: HookId): F[Either[DatabaseError, Unit]] =
    withUnitResult(enableHookQ(id))

  override def takeHook(
    id: HookId,
    personId: PersonId
  ): F[Either[DatabaseError, Unit]] =
    withUnitResult(takeHookQ(id, personId))

  override def dropHook(id: HookId): F[Either[DatabaseError, Unit]] =
    withUnitResult(dropHookQ(id))

}
