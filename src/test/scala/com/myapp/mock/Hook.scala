package com.myapp.mock

import com.myapp.types.IdTypes._

case class Hook(
  wrId: WardrobeId,
  realNumber: Int,
  enabled: Boolean = true,
  isFree: Boolean = true,
  occupiedBy: Option[PersonId] = None
)
