package com.myapp.model.database

import doobie.Read
import java.time.LocalDateTime
import com.myapp.types.IdTypes._

case class PersonInfo(
  id: PersonId,
  orgId: OrgId,
  name: String,
  registeredAt: LocalDateTime
)

object PersonInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val personInfoRead: Read[PersonInfo] =
    Read[(PersonId, OrgId, String, LocalDateTime)].map {
      case (id, orgId, name, registeredAt) =>
        PersonInfo(id, orgId, name, registeredAt)
    }

}
