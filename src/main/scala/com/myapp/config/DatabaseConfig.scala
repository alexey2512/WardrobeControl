package com.myapp.config

import pureconfig._
import pureconfig.generic.semiauto._

case class DatabaseConfig(
  url: String,
  driver: String,
  user: String,
  password: String
)

object DatabaseConfig {

  implicit val databaseConfigReader: ConfigReader[DatabaseConfig] =
    deriveReader[DatabaseConfig]

}
