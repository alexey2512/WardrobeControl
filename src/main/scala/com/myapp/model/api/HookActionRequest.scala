package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._

case class HookActionRequest(currentPosition: Int)

object HookActionRequest {

  implicit val hookActionRequestEncoder: Encoder[HookActionRequest] =
    deriveEncoder

  implicit val hookActionRequestDecoder: Decoder[HookActionRequest] =
    deriveDecoder

  implicit val hookActionRequestSchema: Schema[HookActionRequest] =
    Schema.derived[HookActionRequest]

  implicit val hookActionRequestLoggable: Loggable[HookActionRequest] =
    new DictLoggable[HookActionRequest] {
      override def fields[I, V, R, S](a: HookActionRequest, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("currentPosition", a.currentPosition.toLong, i)

      override def logShow(a: HookActionRequest): String = a.toString
    }

}
