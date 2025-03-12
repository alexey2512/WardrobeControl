package com.myapp.model.database

import doobie.Read
import com.myapp.types.IdTypes._
import java.time.LocalDateTime

case class PersonRegInfo(id: PersonId, registeredAt: LocalDateTime)

object PersonRegInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val personRegInfoRead: Read[PersonRegInfo] =
    Read[(PersonId, LocalDateTime)].map { case (id, registeredAt) =>
      PersonRegInfo(id, registeredAt)
    }

}
