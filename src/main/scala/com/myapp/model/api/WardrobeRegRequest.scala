package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._
import cats.syntax.semigroup._

case class WardrobeRegRequest(name: String, hooksCount: Int)

object WardrobeRegRequest {

  implicit val wardrobeRegRequestEncoder: Encoder[WardrobeRegRequest] =
    deriveEncoder

  implicit val wardrobeRegRequestDecoder: Decoder[WardrobeRegRequest] =
    deriveDecoder

  implicit val wardrobeRegRequestSchema: Schema[WardrobeRegRequest] =
    Schema.derived[WardrobeRegRequest]

  implicit val wardrobeRegRequestLoggable: Loggable[WardrobeRegRequest] =
    new DictLoggable[WardrobeRegRequest] {
      override def fields[I, V, R, S](a: WardrobeRegRequest, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addString("name", a.name, i) |+|
          r.addInt("hooksCount", a.hooksCount, i)

      override def logShow(a: WardrobeRegRequest): String = a.toString
    }

}
