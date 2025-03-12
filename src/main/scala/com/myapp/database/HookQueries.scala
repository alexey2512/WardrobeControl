package com.myapp.database

import com.myapp.model.database._
import com.myapp.types.IdTypes._
import doobie._
import doobie.implicits._

private[database] object HookQueries {

  def insertHooksQ(wrId: WardrobeId, count: Int): ConnectionIO[Int] = {
    val values = (1 to count).map(i => (wrId, i)).toList
    Update[(WardrobeId, Int)](
      "INSERT INTO Hook (wr_id, real_number) VALUES (?, ?)"
    ).updateMany(values)
  }

  def findHookByNumberQ(
    wrId: WardrobeId,
    number: Int
  ): ConnectionIO[Option[HookId]] =
    sql"""
         SELECT id
         FROM Hook
         WHERE wr_id = $wrId AND real_number = $number;
       """.query[HookId].option

  def findHookAssociatedWithPersonQ(
    wrId: WardrobeId,
    occupiedBy: PersonId
  ): ConnectionIO[Option[HookInfo]] =
    sql"""
         SELECT id, real_number
         FROM Hook
         WHERE wr_id = $wrId AND occupied_by = $occupiedBy
         AND enabled AND NOT is_free;
       """.query[HookInfo].option

  def findAllEnabledFreeHooksQ(wrId: WardrobeId): ConnectionIO[List[HookInfo]] =
    sql"""
         SELECT id, real_number
         FROM Hook
         WHERE wr_id = $wrId AND enabled AND is_free;
       """.query[HookInfo].to[List]

  def countAllEnabledHooksQ(wrId: WardrobeId): ConnectionIO[Int] =
    sql"""
         SELECT COUNT(*)
         FROM Hook
         WHERE wr_id = $wrId AND enabled;
       """.query[Int].unique

  def countAllEnabledFreeHooksQ(wrId: WardrobeId): ConnectionIO[Int] =
    sql"""
         SELECT COUNT(*)
         FROM Hook
         WHERE wr_id = $wrId AND enabled AND is_free;
       """.query[Int].unique

  def disableHookQ(id: HookId): ConnectionIO[Int] =
    sql"""
         UPDATE Hook
         SET enabled = FALSE
         WHERE id = $id AND enabled AND is_free;
       """.update.run

  def enableHookQ(id: HookId): ConnectionIO[Int] =
    sql"""
         UPDATE Hook
         SET enabled = TRUE
         WHERE id = $id AND NOT enabled;
       """.update.run

  def takeHookQ(id: HookId, personId: PersonId): ConnectionIO[Int] =
    sql"""
         UPDATE Hook
         SET occupied_by = $personId, is_free = FALSE
         WHERE id = $id AND enabled;
       """.update.run

  def dropHookQ(id: HookId): ConnectionIO[Int] =
    sql"""
         UPDATE Hook
         SET occupied_by = NULL, is_free = TRUE
         WHERE id = $id AND enabled;
       """.update.run

}
