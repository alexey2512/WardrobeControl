package com.myapp.mock

import com.myapp.types.AuthTokenTypes._

import java.time.LocalDateTime

case class Organization(
  name: String,
  address: String,
  token: OrgToken,
  registeredAt: LocalDateTime
)
