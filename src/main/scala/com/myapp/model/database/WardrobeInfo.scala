package com.myapp.model.database

import doobie._
import java.time.LocalDateTime
import com.myapp.types.IdTypes._

case class WardrobeInfo(
  id: WardrobeId,
  orgId: OrgId,
  name: String,
  hooksCount: Int,
  registeredAt: LocalDateTime
)

object WardrobeInfo {
  import doobie.implicits.javatimedrivernative._

  implicit val wardrobeInfoRead: Read[WardrobeInfo] =
    Read[(WardrobeId, OrgId, String, Int, LocalDateTime)].map {
      case (id, orgId, name, hooksCount, registeredAt) =>
        WardrobeInfo(id, orgId, name, hooksCount, registeredAt)
    }

}
