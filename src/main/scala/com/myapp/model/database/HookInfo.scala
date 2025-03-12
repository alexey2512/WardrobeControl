package com.myapp.model.database

import doobie._
import com.myapp.types.IdTypes._

case class HookInfo(id: HookId, number: Int)

object HookInfo {

  implicit val hookInfoRead: Read[HookInfo] =
    Read[(HookId, Int)].map { case (id, number) => HookInfo(id, number) }

}
