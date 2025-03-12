package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import tofu.logging._
import java.time.LocalDateTime

case class OrgRegResponse(
  id: OrgId,
  token: OrgToken,
  registeredAt: LocalDateTime
)

object OrgRegResponse {
  import LocalDateTimeImplicits._

  implicit val orgRegResponseEncoder: Encoder[OrgRegResponse] =
    deriveEncoder

  implicit val orgRegResponseDecoder: Decoder[OrgRegResponse] =
    deriveDecoder

  implicit val orgRegResponseSchema: Schema[OrgRegResponse] =
    Schema.derived[OrgRegResponse]

  implicit val orgRegResponseLoggable: Loggable[OrgRegResponse] =
    new DictLoggable[OrgRegResponse] {
      override def fields[I, V, R, S](a: OrgRegResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i)

      override def logShow(a: OrgRegResponse): String = a.toString
    }

}
