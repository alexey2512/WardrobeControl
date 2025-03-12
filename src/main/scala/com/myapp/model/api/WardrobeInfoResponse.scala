package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.IdTypes._
import java.time.LocalDateTime
import tofu.logging._
import cats.syntax.semigroup._

case class WardrobeInfoResponse(
  id: WardrobeId,
  name: String,
  hooksCount: Int,
  registeredAt: LocalDateTime,
  enabledHooksCount: Int = 0,
  enabledFreeHooksCount: Int = 0
)

object WardrobeInfoResponse {
  import LocalDateTimeImplicits._

  implicit val wardrobeInfoResponseEncoder: Encoder[WardrobeInfoResponse] =
    deriveEncoder

  implicit val wardrobeInfoResponseDecoder: Decoder[WardrobeInfoResponse] =
    deriveDecoder

  implicit val wardrobeInfoResponseSchema: Schema[WardrobeInfoResponse] =
    Schema.derived[WardrobeInfoResponse]

  implicit val wardrobeInfoResponseLoggable: Loggable[WardrobeInfoResponse] =
    new DictLoggable[WardrobeInfoResponse] {
      override def fields[I, V, R, S](a: WardrobeInfoResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i) |+|
          r.addString("name", a.name, i) |+|
          r.addInt("hooksCount", a.hooksCount, i) |+|
          r.addInt("enabledHooksCount", a.enabledHooksCount, i) |+|
          r.addInt("enabledFreeHooksCount", a.enabledFreeHooksCount, i)

      override def logShow(a: WardrobeInfoResponse): String = a.toString
    }

}
