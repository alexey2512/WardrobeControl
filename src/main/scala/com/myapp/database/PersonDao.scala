package com.myapp.database

import com.myapp.error.DatabaseError
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

trait PersonDao[F[_]] {

  def insertPerson(
    orgId: OrgId,
    name: String,
    token: PersonToken
  ): F[Either[DatabaseError, PersonRegInfo]]

  def findPerson(token: PersonToken): F[Either[DatabaseError, PersonInfo]]

  def deletePerson(id: PersonId): F[Either[DatabaseError, Unit]]

  def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]]

}
