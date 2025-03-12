package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import java.time.LocalDateTime
import tofu.logging._

case class WardrobeRegResponse(
  id: WardrobeId,
  token: WardrobeToken,
  registeredAt: LocalDateTime
)

object WardrobeRegResponse {
  import LocalDateTimeImplicits._

  implicit val wardrobeRegResponseEncoder: Encoder[WardrobeRegResponse] =
    deriveEncoder

  implicit val wardrobeRegResponseDecoder: Decoder[WardrobeRegResponse] =
    deriveDecoder

  implicit val wardrobeRegResponseSchema: Schema[WardrobeRegResponse] =
    Schema.derived[WardrobeRegResponse]

  implicit val wardrobeRegResponseLoggable: Loggable[WardrobeRegResponse] =
    new DictLoggable[WardrobeRegResponse] {
      override def fields[I, V, R, S](a: WardrobeRegResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i)

      override def logShow(a: WardrobeRegResponse): String = a.toString
    }

}
