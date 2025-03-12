package com.myapp.config

import pureconfig._
import pureconfig.generic.semiauto._

case class AppConfig(
  name: String,
  version: String,
  server: ServerConfig,
  database: DatabaseConfig
)

object AppConfig {

  implicit val appConfigReader: ConfigReader[AppConfig] =
    deriveReader[AppConfig]

}
