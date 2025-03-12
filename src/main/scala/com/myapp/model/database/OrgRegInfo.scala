package com.myapp.model.database

import doobie.Read
import com.myapp.types.IdTypes._
import java.time.LocalDateTime

case class OrgRegInfo(id: OrgId, registeredAt: LocalDateTime)

object OrgRegInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val orgRegInfoRead: Read[OrgRegInfo] =
    Read[(OrgId, LocalDateTime)].map { case (id, registeredAt) =>
      OrgRegInfo(id, registeredAt)
    }

}
