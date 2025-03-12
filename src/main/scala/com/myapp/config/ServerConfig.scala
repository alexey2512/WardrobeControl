package com.myapp.config

import pureconfig._
import pureconfig.generic.semiauto._

case class ServerConfig(host: String, port: Int)

object ServerConfig {

  implicit val serverConfigReader: ConfigReader[ServerConfig] =
    deriveReader[ServerConfig]

}
