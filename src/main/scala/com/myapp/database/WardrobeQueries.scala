package com.myapp.database

import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie._
import doobie.implicits._

private[database] object WardrobeQueries {

  def insertWardrobeQ(
    orgId: OrgId,
    name: String,
    hooksCount: Int,
    token: WardrobeToken
  ): ConnectionIO[WardrobeRegInfo] =
    sql"""
        INSERT INTO Wardrobe (org_id, name, hooks_count, token)
        VALUES ($orgId, $name, $hooksCount, $token)
        RETURNING id, registration_datetime;
       """.query[WardrobeRegInfo].unique

  def findWardrobeQ(token: WardrobeToken): ConnectionIO[Option[WardrobeInfo]] =
    sql"""
         SELECT id, org_id, name, hooks_count, registration_datetime
         FROM Wardrobe
         WHERE token = $token;
       """.query[WardrobeInfo].option

  def deleteWardrobeQ(id: WardrobeId): ConnectionIO[Int] =
    sql"""
         DELETE FROM Wardrobe
         WHERE id = $id;
       """.update.run

  def countInOrgQ(orgId: OrgId): ConnectionIO[Int] =
    sql"""
         SELECT COUNT(*)
         FROM Wardrobe
         WHERE org_id = $orgId;
       """.query[Int].unique

}
