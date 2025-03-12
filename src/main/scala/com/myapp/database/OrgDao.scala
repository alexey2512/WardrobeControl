package com.myapp.database

import com.myapp.error.DatabaseError
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

trait OrgDao[F[_]] {

  def insertOrg(
    name: String,
    address: String,
    token: OrgToken
  ): F[Either[DatabaseError, OrgRegInfo]]

  def findOrg(token: OrgToken): F[Either[DatabaseError, OrgInfo]]

  def deleteOrg(id: OrgId): F[Either[DatabaseError, Unit]]

}
