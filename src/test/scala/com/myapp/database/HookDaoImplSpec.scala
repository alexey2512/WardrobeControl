package com.myapp.database

import cats.data.EitherT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import com.myapp.business.Tokens
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

@DoNotDiscover
class HookDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val txr: Resource[IO, HikariTransactor[IO]] =
    new CreateTransactorImpl[IO].createTransactor

  val orgDao: OrgDao[IO]           = new OrgDaoImpl[IO](txr)
  val personDao: PersonDao[IO]     = new PersonDaoImpl[IO](txr)
  val wardrobeDao: WardrobeDao[IO] = new WardrobeDaoImpl[IO](txr)
  val hookDao: HookDao[IO]         = new HookDaoImpl[IO](txr)
  val tokens: Tokens[IO]           = Tokens[IO]

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

  import orgDao._
  import personDao._
  import wardrobeDao._
  import hookDao._

  private def findHook(number: Int): HookId =
    findHookByNumber(warId, number).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(hid)  => hid
    }

  override def beforeAll(): Unit = {
    super.beforeAll()
    (for {
      org <- EitherT(insertOrg(orgName, orgAddress, orgToken)).map(_.id)
      per <- EitherT(insertPerson(org, perName, perToken)).map(_.id)
      war <- EitherT(insertWardrobe(org, warName, hooksCount, warToken))
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
    checkSuccess[DE, Unit](insertHooks(warId, hooksCount), _ => succeed)
  }

  "findHookByNumber" should "find hook if it exists" in {
    checkSuccess[DE, HookId](findHookByNumber(warId, 1), _ => succeed)
  }

  it should "fail with error if hook doesn't exist" in {
    checkError[DE, NotFoundErrorD, HookId](findHookByNumber(warId, 4))
  }

  it should "fail with error if referenced wardrobe doesn't exist" in {
    checkError[DE, NotFoundErrorD, HookId](findHookByNumber(-1, 1))
  }

  "takeDropComposition" should "perform successfully" in {
    val number: Int = 1
    val id: HookId  = findHook(number)
    checkSuccess[DE, Unit](takeHook(id, perId), _ => succeed)
    checkSuccess[DE, HookInfo](
      findHookAssociatedWithPerson(warId, perId),
      _.number shouldEqual number
    )
    checkSuccess[DE, List[HookInfo]](
      findAllEnabledFreeHooks(warId),
      _.map(_.number) shouldEqual (1 to hooksCount).filterNot(_ == number)
    )
    checkSuccess[DE, Int](
      countAllEnabledHooks(warId),
      _ shouldEqual hooksCount
    )
    checkSuccess[DE, Int](
      countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](dropHook(id), _ => succeed)
  }

  "enableDisableComposition" should "perform successfully" in {
    val first: Int  = 1
    val second: Int = 2
    val id1: HookId = findHook(first)
    val id2: HookId = findHook(second)
    checkSuccess[DE, Unit](disableHook(id1), _ => succeed)
    checkError[DE, UnprocessableEntityErrorD, Unit](disableHook(id1))
    checkSuccess[DE, Int](
      countAllEnabledHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](takeHook(id2, perId), _ => succeed)
    checkError[DE, UnprocessableEntityErrorD, Unit](disableHook(id2))
    checkSuccess[DE, Int](
      countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 2
    )
    checkSuccess[DE, Unit](enableHook(id1), _ => succeed)
    checkSuccess[DE, Int](
      countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount - 1
    )
    checkSuccess[DE, Unit](dropHook(id2), _ => succeed)
    checkSuccess[DE, Int](
      countAllEnabledFreeHooks(warId),
      _ shouldEqual hooksCount
    )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    deleteOrg(orgId).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(_)    => ()
    }
  }

}
