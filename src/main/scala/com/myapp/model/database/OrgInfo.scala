package com.myapp.model.database

import doobie.Read
import com.myapp.types.IdTypes._
import java.time.LocalDateTime

case class OrgInfo(
  id: OrgId,
  name: String,
  address: String,
  registeredAt: LocalDateTime
)

object OrgInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val orgInfoRead: Read[OrgInfo] =
    Read[(OrgId, String, String, LocalDateTime)].map {
      case (id, name, address, registeredAt) =>
        OrgInfo(id, name, address, registeredAt)
    }

}
