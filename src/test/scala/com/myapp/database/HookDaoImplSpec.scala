package com.myapp.database

import cats.data.EitherT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import com.myapp.business.Tokens
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class HookDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val env                 = new DataAccessEnvironment
  var od: OrgDao[IO]      = null
  var pd: PersonDao[IO]   = null
  var wd: WardrobeDao[IO] = null
  var hd: HookDao[IO]     = null
  val tokens: Tokens[IO]  = Tokens[IO]

  var orgId: OrgId       = 0
  val orgName: String    = "test org"
  val orgAddress: String = "*****"
  val orgToken: OrgToken = genOrgToken(orgName)

  var perId: PersonId       = 0
  val perName: String       = "test person"
  val perToken: PersonToken = genPerToken(perName)

  var warId: WardrobeId       = 0
  val warName: String         = "test wardrobe"
  val hooksCount: Int         = 3
  val warToken: WardrobeToken = genWarToken(warName)

  private def findHook(number: Int): HookId =
    hd.findHookByNumber(warId, number).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(hid)  => hid
    }

  override def beforeAll(): Unit = {
    super.beforeAll()
    env.start()
    od = env.makeOrgDao
    pd = env.makePersonDao
    wd = env.makeWardrobeDao
    hd = env.makeHookDao
    (for {
      org <- EitherT(od.insertOrg(orgName, orgAddress, orgToken)).map(_.id)
      per <- EitherT(pd.insertPerson(org, perName, perToken)).map(_.id)
      war <- EitherT(wd.insertWardrobe(org, warName, hooksCount, warToken))
        .map(_.id)
    } yield (org, per, war)).value.unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right((org, per, war)) =>
        orgId = org
        perId = per
        warId = war
    }
  }

  "insertHooks" should "correctly insert N new hooks to db" in {
    checkSuccess[DE, Unit](hd.insertHooks(warId, hooksCount), _ => succeed)
  }

  "findHookByNumber" should "find hook if it exists" in {
    checkSuccess[DE, HookId](hd.findHookByNumber(warId, 1), _ => succeed)
  }

  it should "fail with error if hook doesn't exist" in {
    checkError[DE, NotFoundErrorD, HookId](hd.findHookByNumber(warId, 4))
  }

  it should "fail with error if referenced wardrobe doesn't exist" in {
    checkError[DE, NotFoundErrorD, HookId](hd.findHookByNumber(-1, 1))
  }

  "takeDropComposition" should "perform successfully" in {
    val number: Int = 1
    val id: HookId  = findHook(number)
    checkSuccess[DE, Unit](hd.takeHook(id, perId), _ => succeed)
    checkSuccess[DE, HookInfo](
      hd.findHookAssociatedWithPerson(warId, perId),
      _.number shouldEqual number
    )
    checkSuccess[DE, List[HookInfo]](
      hd.findAllEnabledFreeHooks(warId),
      _.map(_.number) shouldEqual (1 to hooksCount).filterNot(_ == number)
    )
    checkSuccess[DE, Int](
      hd.countAllEnabledHooks(warId),
      _ shouldEqual hooksCount
    )
    checkSuccess[DE, Int](
      hd.countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](hd.dropHook(id), _ => succeed)
  }

  "enableDisableComposition" should "perform successfully" in {
    val first: Int  = 1
    val second: Int = 2
    val id1: HookId = findHook(first)
    val id2: HookId = findHook(second)
    checkSuccess[DE, Unit](hd.disableHook(id1), _ => succeed)
    checkError[DE, UnprocessableEntityErrorD, Unit](hd.disableHook(id1))
    checkSuccess[DE, Int](
      hd.countAllEnabledHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](hd.takeHook(id2, perId), _ => succeed)
    checkError[DE, UnprocessableEntityErrorD, Unit](hd.disableHook(id2))
    checkSuccess[DE, Int](
      hd.countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 2
    )
    checkSuccess[DE, Unit](hd.enableHook(id1), _ => succeed)
    checkSuccess[DE, Int](
      hd.countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](hd.dropHook(id2), _ => succeed)
    checkSuccess[DE, Int](
      hd.countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount
    )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    od.deleteOrg(orgId).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(_)    => ()
    }
    env.finish()
  }

}
