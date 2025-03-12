package com.myapp.model.api

import sttp.tapir.Schema
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeImplicits {

  implicit val localDateTimeEncoder: Encoder[LocalDateTime] =
    Encoder.encodeString.contramap[LocalDateTime](
      _.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] =
    Decoder.decodeString.map[LocalDateTime](
      LocalDateTime.parse(_, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )

  implicit val localDateTimeSchema: Schema[LocalDateTime] =
    Schema.schemaForLocalDateTime

}
