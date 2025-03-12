package com.myapp.database

import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie._
import doobie.implicits._

private[database] object OrgQueries {

  def insertOrgQ(
    name: String,
    address: String,
    token: OrgToken
  ): ConnectionIO[OrgRegInfo] =
    sql"""
          INSERT INTO Organization (name, address, token)
          VALUES ($name, $address, $token)
          RETURNING id, registration_datetime;
    """.query[OrgRegInfo].unique

  def findOrgQ(token: OrgToken): ConnectionIO[Option[OrgInfo]] =
    sql"""
         SELECT id, name, address, registration_datetime
         FROM Organization
         WHERE token = $token
       """.query[OrgInfo].option

  def deleteOrgQ(id: OrgId): ConnectionIO[Int] =
    sql"""
         DELETE FROM Organization
         WHERE id = $id;
       """.update.run

}
