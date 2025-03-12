package com.myapp.database

import com.myapp.error.DatabaseError
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

trait WardrobeDao[F[_]] {

  def insertWardrobe(
    orgId: OrgId,
    name: String,
    hooksCount: Int,
    token: WardrobeToken
  ): F[Either[DatabaseError, WardrobeRegInfo]]

  def findWardrobe(token: WardrobeToken): F[Either[DatabaseError, WardrobeInfo]]

  def deleteWardrobe(id: WardrobeId): F[Either[DatabaseError, Unit]]

  def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]]
}
