package com.myapp.business

import cats.effect.IO
import com.myapp.SpecCommons._
import com.myapp.database._
import com.myapp.mock._
import com.myapp.types.IdTypes._

trait BusinessMockInit {

  val tokens: Tokens[IO] = Tokens[IO]

  val hookDao: HookDaoMock[IO] = new HookDaoMock[IO](makeRef[HookId, Hook])

  val personDao: PersonDaoMock[IO] =
    new PersonDaoMock[IO](makeRef[PersonId, Person])

  val wardrobeDao: WardrobeDaoMock[IO] =
    new WardrobeDaoMock[IO](makeRef[WardrobeId, Wardrobe], hookDao)

  val orgDao: OrgDao[IO] =
    new OrgDaoMock[IO](makeRef[OrgId, Organization], personDao, wardrobeDao)

  val orgLogic: OrgLogic[IO] =
    new OrgLogicImpl[IO](tokens, orgDao, personDao, wardrobeDao)

  val personLogic: PersonLogic[IO] =
    new PersonLogicImpl[IO](tokens, orgDao, personDao)

  val wardrobeLogic: WardrobeLogic[IO] =
    new WardrobeLogicImpl[IO](tokens, orgDao, wardrobeDao, hookDao)

  val hookLogic: HookLogic[IO] =
    new HookLogicImpl[IO](tokens, personDao, wardrobeDao, hookDao)

}
