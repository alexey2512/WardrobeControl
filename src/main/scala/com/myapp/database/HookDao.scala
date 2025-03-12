package com.myapp.database

import com.myapp.error.DatabaseError
import com.myapp.model.database._
import com.myapp.types.IdTypes._

trait HookDao[F[_]] {

  def insertHooks(wrId: WardrobeId, count: Int): F[Either[DatabaseError, Unit]]

  def findHookByNumber(
    wrId: WardrobeId,
    number: Int
  ): F[Either[DatabaseError, HookId]]

  def findHookAssociatedWithPerson(
    wrId: WardrobeId,
    occupiedBy: PersonId
  ): F[Either[DatabaseError, HookInfo]]

  def findAllEnabledFreeHooks(
    wrId: WardrobeId
  ): F[Either[DatabaseError, List[HookInfo]]]

  def countAllEnabledHooks(wrId: WardrobeId): F[Either[DatabaseError, Int]]

  def countAllEnabledFreeHooks(wrId: WardrobeId): F[Either[DatabaseError, Int]]

  def disableHook(id: HookId): F[Either[DatabaseError, Unit]]

  def enableHook(id: HookId): F[Either[DatabaseError, Unit]]

  def takeHook(id: HookId, personId: PersonId): F[Either[DatabaseError, Unit]]

  def dropHook(id: HookId): F[Either[DatabaseError, Unit]]

}
