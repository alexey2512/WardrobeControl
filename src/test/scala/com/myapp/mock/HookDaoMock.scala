package com.myapp.mock

import cats.Monad
import cats.effect.Ref
import cats.syntax.functor._
import com.myapp.database.HookDao
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.IdTypes._

class HookDaoMock[F[_]: Monad](
  ref: Ref[F, Map[HookId, Hook]]
) extends HookDao[F] {

  override def insertHooks(
    wrId: WardrobeId,
    count: OrgId
  ): F[Either[DatabaseError, Unit]] =
    ref.modify { state =>
      val nextId: HookId = state.keys.maxOption.getOrElse(-1) + 1
      val newHooks =
        (1 to count).map(i => nextId + i - 1 -> Hook(wrId, i)).toMap
      (state ++ newHooks, Right(()))
    }

  override def findHookByNumber(
    wrId: WardrobeId,
    number: OrgId
  ): F[Either[DatabaseError, HookId]] =
    ref.get.map(
      _.find { case (_, hook) =>
        hook.wrId == wrId && hook.realNumber == number
      } match {
        case Some((id, _)) => Right(id)
        case None          => Left(NotFoundErrorD("no such hook"))
      }
    )

  override def findHookAssociatedWithPerson(
    wrId: WardrobeId,
    occupiedBy: PersonId
  ): F[Either[DatabaseError, HookInfo]] =
    ref.get.map(
      _.find { case (_, hook) =>
        hook.wrId == wrId && hook.occupiedBy == (Some(occupiedBy): Option[
          PersonId
        ])
      } match {
        case Some((id, hook)) => Right(HookInfo(id, hook.realNumber))
        case None             => Left(NotFoundErrorD("no such hook"))
      }
    )

  override def findAllEnabledFreeHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, List[HookInfo]]] =
    ref.get.map(state =>
      Right(
        state
          .filter { case (_, hook) =>
            hook.wrId == wrId && hook.enabled && hook.isFree
          }
          .toList
          .map { case (id, hook) => HookInfo(id, hook.realNumber) }
      )
    )

  private def countAllHooks(
    wrId: WardrobeId,
    p: Hook => Boolean
  ): F[Either[DatabaseError, Int]] =
    ref.get.map(state =>
      Right(
        state.count { case (_, hook) =>
          hook.wrId == wrId && hook.enabled && p(hook)
        }
      )
    )

  override def countAllEnabledHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, Int]] =
    countAllHooks(wrId, _ => true)

  override def countAllEnabledFreeHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, Int]] =
    countAllHooks(wrId, _.enabled)

  private def updateHook(
    id: HookId,
    enabledValue: Boolean,
    p: Hook => Boolean,
    update: Hook => Hook
  ): F[Either[DatabaseError, Unit]] =
    ref.modify { state =>
      val hook: Option[Hook] = state
        .find { case (id1, hook) =>
          id1 == id && hook.enabled == enabledValue && p(hook)
        }
        .map(_._2)
      hook match {
        case None =>
          (
            state,
            Left(UnprocessableEntityErrorD("unable to perform operation"))
          )
        case Some(value) =>
          (
            state.updated(id, update(value)),
            Right(())
          )
      }
    }

  override def disableHook(id: HookId): F[Either[DatabaseError, Unit]] =
    updateHook(id, enabledValue = true, _.isFree, _.copy(enabled = false))

  override def enableHook(id: HookId): F[Either[DatabaseError, Unit]] =
    updateHook(id, enabledValue = false, _.isFree, _.copy(enabled = true))

  override def takeHook(
    id: HookId,
    personId: PersonId
  ): F[Either[DatabaseError, Unit]] =
    updateHook(
      id,
      enabledValue = true,
      _ => true,
      _.copy(isFree = false, occupiedBy = Some(personId))
    )

  override def dropHook(id: HookId): F[Either[DatabaseError, Unit]] =
    updateHook(
      id,
      enabledValue = true,
      _ => true,
      _.copy(isFree = true, occupiedBy = None)
    )

  def deleteAllInWardrobe(wrId: WardrobeId): F[Either[DatabaseError, Unit]] =
    ref.modify(state =>
      (state.filterNot { case (_, hook) => hook.wrId == wrId }, Right(()))
    )

}
