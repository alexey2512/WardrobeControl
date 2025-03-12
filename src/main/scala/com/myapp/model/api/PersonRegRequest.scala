package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._

case class PersonRegRequest(name: String)

object PersonRegRequest {

  implicit val personRegRequestEncoder: Encoder[PersonRegRequest] =
    deriveEncoder

  implicit val personRegRequestDecoder: Decoder[PersonRegRequest] =
    deriveDecoder

  implicit val personRegRequestSchema: Schema[PersonRegRequest] =
    Schema.derived[PersonRegRequest]

  implicit val personRegRequestLoggable: Loggable[PersonRegRequest] =
    new DictLoggable[PersonRegRequest] {
      override def fields[I, V, R, S](a: PersonRegRequest, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addString("name", a.name, i)

      override def logShow(a: PersonRegRequest): String = a.toString
    }

}
