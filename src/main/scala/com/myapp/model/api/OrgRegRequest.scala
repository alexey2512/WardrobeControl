package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._
import cats.syntax.semigroup._

case class OrgRegRequest(name: String, address: String)

object OrgRegRequest {

  implicit val orgRegRequestEncoder: Encoder[OrgRegRequest] =
    deriveEncoder

  implicit val orgRegRequestDecoder: Decoder[OrgRegRequest] =
    deriveDecoder

  implicit val orgRegRequestSchema: Schema[OrgRegRequest] =
    Schema.derived[OrgRegRequest]

  implicit val orgRegRequestLoggable: Loggable[OrgRegRequest] =
    new DictLoggable[OrgRegRequest] {
      override def fields[I, V, R, S](a: OrgRegRequest, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addString("name", a.name, i) |+|
          r.addString("address", a.address, i)

      override def logShow(a: OrgRegRequest): String = a.toString
    }

}
