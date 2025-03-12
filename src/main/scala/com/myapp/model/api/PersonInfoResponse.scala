package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.IdTypes._
import tofu.logging._
import cats.syntax.semigroup._
import java.time.LocalDateTime

case class PersonInfoResponse(
  id: PersonId,
  name: String,
  registeredAt: LocalDateTime
)

object PersonInfoResponse {
  import LocalDateTimeImplicits._

  implicit val personInfoResponseEncoder: Encoder[PersonInfoResponse] =
    deriveEncoder

  implicit val personInfoResponseDecoder: Decoder[PersonInfoResponse] =
    deriveDecoder

  implicit val personInfoResponseSchema: Schema[PersonInfoResponse] =
    Schema.derived[PersonInfoResponse]

  implicit val personInfoResponseLoggable: Loggable[PersonInfoResponse] =
    new DictLoggable[PersonInfoResponse] {
      override def fields[I, V, R, S](a: PersonInfoResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i) |+|
          r.addString("name", a.name, i)

      override def logShow(a: PersonInfoResponse): String = a.toString
    }

}
