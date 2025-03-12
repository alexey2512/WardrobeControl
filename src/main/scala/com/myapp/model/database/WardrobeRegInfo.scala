package com.myapp.model.database

import doobie.Read
import com.myapp.types.IdTypes._
import java.time.LocalDateTime

case class WardrobeRegInfo(id: WardrobeId, registeredAt: LocalDateTime)

object WardrobeRegInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val wardrobeRegInfoRead: Read[WardrobeRegInfo] =
    Read[(WardrobeId, LocalDateTime)].map { case (id, registeredAt) =>
      WardrobeRegInfo(id, registeredAt)
    }

}
