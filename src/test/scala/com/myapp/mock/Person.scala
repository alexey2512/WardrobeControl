package com.myapp.mock

import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

import java.time.LocalDateTime

case class Person(
  orgId: OrgId,
  name: String,
  token: PersonToken,
  registeredAt: LocalDateTime
)
