package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.myapp.types.IdTypes._
import java.time.LocalDateTime
import tofu.logging._
import cats.syntax.semigroup._

case class OrgInfoResponse(
  id: OrgId,
  name: String,
  address: String,
  registeredAt: LocalDateTime,
  persons: Int = 0,
  wardrobes: Int = 0
)

object OrgInfoResponse {
  import LocalDateTimeImplicits._

  implicit val orgInfoResponseEncoder: Encoder[OrgInfoResponse] =
    deriveEncoder

  implicit val orgInfoResponseDecoder: Decoder[OrgInfoResponse] =
    deriveDecoder

  implicit val orgInfoResponseSchema: Schema[OrgInfoResponse] =
    Schema.derived[OrgInfoResponse]

  implicit val orgInfoResponseLoggable: Loggable[OrgInfoResponse] =
    new DictLoggable[OrgInfoResponse] {
      override def fields[I, V, R, S](a: OrgInfoResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("id", a.id.toLong, i) |+|
          r.addString("name", a.name, i) |+|
          r.addString("address", a.address, i) |+|
          r.addInt("persons", a.persons, i) |+|
          r.addInt("wardrobes", a.wardrobes, i)

      override def logShow(a: OrgInfoResponse): String = a.toString
    }

}
