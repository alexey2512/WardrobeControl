package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import java.time.LocalDateTime
import tofu.logging._

case class PersonRegResponse(
  id: PersonId,
  token: PersonToken,
  registeredAt: LocalDateTime
)

object PersonRegResponse {
  import LocalDateTimeImplicits._

  implicit val personRegResponseEncoder: Encoder[PersonRegResponse] =
    deriveEncoder

  implicit val personRegResponseDecoder: Decoder[PersonRegResponse] =
    deriveDecoder

  implicit val personRegResponseSchema: Schema[PersonRegResponse] =
    Schema.derived[PersonRegResponse]

  implicit val personRegResponseLoggable: Loggable[PersonRegResponse] =
    new DictLoggable[PersonRegResponse] {
      override def fields[I, V, R, S](a: PersonRegResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i)

      override def logShow(a: PersonRegResponse): String = a.toString
    }

}
