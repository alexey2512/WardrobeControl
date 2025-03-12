package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._

case class HookNumberRequest(number: Int)

object HookNumberRequest {

  implicit val hookNumberRequestEncoder: Encoder[HookNumberRequest] =
    deriveEncoder

  implicit val hookNumberRequestDecoder: Decoder[HookNumberRequest] =
    deriveDecoder

  implicit val hookNumberRequestSchema: Schema[HookNumberRequest] =
    Schema.derived[HookNumberRequest]

  implicit val hookNumberRequestLoggable: Loggable[HookNumberRequest] =
    new DictLoggable[HookNumberRequest] {
      override def fields[I, V, R, S](a: HookNumberRequest, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("number", a.number.toLong, i)

      override def logShow(a: HookNumberRequest): String = a.toString
    }

}
