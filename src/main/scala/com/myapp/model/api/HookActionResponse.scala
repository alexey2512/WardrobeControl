package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import tofu.logging._
import cats.syntax.semigroup._

case class HookActionResponse(moveTo: Int, toTakeHook: Boolean)

object HookActionResponse {

  implicit val hookActionResponseEncoder: Encoder[HookActionResponse] =
    deriveEncoder

  implicit val hookActionResponseDecoder: Decoder[HookActionResponse] =
    deriveDecoder

  implicit val hookActionResponseSchema: Schema[HookActionResponse] =
    Schema.derived[HookActionResponse]

  implicit val hookActionResponseLoggable: Loggable[HookActionResponse] =
    new DictLoggable[HookActionResponse] {
      override def fields[I, V, R, S](a: HookActionResponse, i: I)(implicit
        r: LogRenderer[I, V, R, S]
      ): R =
        r.addInt("moveTo", a.moveTo, i) |+|
          r.addBool("toTakeHook", a.toTakeHook, i)

      override def logShow(a: HookActionResponse): String = a.toString
    }

}
