package com.myapp.database

import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie._
import doobie.implicits._

private[database] object PersonQueries {

  def insertPersonQ(
    orgId: OrgId,
    name: String,
    token: PersonToken
  ): ConnectionIO[PersonRegInfo] =
    sql"""
         INSERT INTO Person (org_id, name, token)
         VALUES ($orgId, $name, $token)
         RETURNING id, registration_datetime;
       """.query[PersonRegInfo].unique

  def findPersonQ(token: PersonToken): ConnectionIO[Option[PersonInfo]] =
    sql"""
         SELECT id, org_id, name, registration_datetime
         FROM Person
         WHERE token = $token;
       """.query[PersonInfo].option

  def deletePersonQ(id: PersonId): ConnectionIO[Int] =
    sql"""
         DELETE FROM Person
         WHERE id = $id;
       """.update.run

  def countInOrgQ(orgId: OrgId): ConnectionIO[Int] =
    sql"""
         SELECT COUNT(*)
         FROM Person
         WHERE org_id = $orgId;
       """.query[Int].unique

}
