package com.myapp.mock

import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

import java.time.LocalDateTime

case class Wardrobe(
  orgId: OrgId,
  name: String,
  hooksCount: Int,
  token: WardrobeToken,
  registeredAt: LocalDateTime
)
