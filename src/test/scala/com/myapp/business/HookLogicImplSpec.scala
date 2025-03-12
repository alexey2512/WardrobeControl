package com.myapp.business

import cats.data.EitherT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.implicitConversions

class HookLogicImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll
    with InitBusinessOnMock {

  var org: OrgInfoResponse = OrgInfoResponse(0, "test org", "*****", null)
  var orgToken: OrgToken   = ""

  var per1: PersonInfoResponse = PersonInfoResponse(0, "test person 1", null)
  var per1Token: PersonToken   = ""

  var per2: PersonInfoResponse = PersonInfoResponse(0, "test person 2", null)
  var per2Token: PersonToken   = ""

  var war: WardrobeInfoResponse =
    WardrobeInfoResponse(0, "test wardrobe", 4, null)

  var warToken: WardrobeToken = ""

  import orgLogic._
  import personLogic._
  import wardrobeLogic._
  import hookLogic._

  implicit def toHookNumberRequest(num: Int): HookNumberRequest =
    HookNumberRequest(num)

  implicit def toHookActionRequest(pos: Int): HookActionRequest =
    HookActionRequest(pos)

  private def ableHookCheck(
    security: WardrobeToken => IO[Either[AE, WardrobeId]],
    business: (WardrobeId, HookNumberRequest) => IO[Either[AE, Unit]]
  ): Assertion = {
    checkSuccess[AE, WardrobeId](security(warToken), _ shouldEqual war.id)
    checkError[AE, UnauthorizedError, WardrobeId](security(invalid))
    checkError[AE, ForbiddenError, WardrobeId](security(genPerToken(invalid)))
    checkSuccess[AE, Unit](business(war.id, 1), _ => succeed)
    checkError[AE, UnprocessableEntityError, Unit](business(war.id, 1))
    checkError[AE, BadRequestError, Unit](business(war.id, -1))
    checkError[AE, NotFoundError, Unit](business(war.id, 14))
    checkError[AE, NotFoundError, Unit](business(-1, 1))
  }

  private def checkSuccessfulAction(
    per: PersonId,
    posFrom: Int,
    expectedPos: Int,
    toTake: Boolean
  ): Assertion =
    checkSuccess[AE, HookActionResponse](
      hookActionBusinessLogic(war.id, per, posFrom),
      r => {
        r.moveTo shouldEqual expectedPos
        r.toTakeHook shouldEqual toTake
      }
    )

  override def beforeAll(): Unit = {
    super.beforeAll()
    (for {
      org <- EitherT(orgRegBusinessLogic(OrgRegRequest(org.name, org.address)))
      war <- EitherT(
        wardrobeRegBusinessLogic(
          org.id,
          WardrobeRegRequest(war.name, war.hooksCount)
        )
      )
      per1 <- EitherT(
        personRegBusinessLogic(org.id, PersonRegRequest(per1.name))
      )
      per2 <- EitherT(
        personRegBusinessLogic(org.id, PersonRegRequest(per2.name))
      )
    } yield (org, war, per1, per2)).value.unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right((o, w, p1, p2)) =>
        org = org.copy(id = o.id, registeredAt = o.registeredAt)
        orgToken = o.token
        war = war.copy(id = w.id, registeredAt = w.registeredAt)
        warToken = w.token
        per1 = per1.copy(id = p1.id, registeredAt = p1.registeredAt)
        per1Token = p1.token
        per2 = per2.copy(id = p2.id, registeredAt = p2.registeredAt)
        per2Token = p2.token
    }
  }

  "disableHook" should "perform correctly" in {
    ableHookCheck(disableHookSecurityLogic, disableHookBusinessLogic)
  }

  "enableHook" should "perform correctly" in {
    ableHookCheck(enableHookSecurityLogic, enableHookBusinessLogic)
  }

  "hookActionSecurityLogic" should "accept correct tokens" in {
    checkSuccess[AE, (WardrobeId, PersonId)](
      hookActionSecurityLogic(warToken, per1Token),
      { case (w, p) =>
        w shouldEqual war.id
        p shouldEqual per1.id
      }
    )
  }

  it should "fail with UnauthorizedError if person token is invalid" in {
    checkError[AE, UnauthorizedError, (WardrobeId, PersonId)](
      hookActionSecurityLogic(warToken, invalid)
    )
  }

  it should "fail with ForbiddenError if token role is not a person" in {
    checkError[AE, ForbiddenError, (WardrobeId, PersonId)](
      hookActionSecurityLogic(warToken, genWarToken(invalid))
    )
  }

  it should "fail with UnauthorizedError if wardrobe token is invalid" in {
    checkError[AE, UnauthorizedError, (WardrobeId, PersonId)](
      hookActionSecurityLogic(invalid, per1Token)
    )
  }

  it should "fail with ForbiddenError if token role is not a wardrobe" in {
    checkError[AE, ForbiddenError, (WardrobeId, PersonId)](
      hookActionSecurityLogic(genPerToken(invalid), per1Token)
    )
  }

  "composition1" should "perform correctly" in {
    checkSuccessfulAction(per1.id, 4, 4, toTake = true)
    checkSuccessfulAction(per2.id, 4, 3, toTake = true)
    checkSuccess[AE, Unit](
      EitherT(disableHookBusinessLogic(war.id, 1))
        .flatMap(_ => EitherT(disableHookBusinessLogic(war.id, 2)))
        .value,
      _ => succeed
    )
    checkError[AE, UnprocessableEntityError, Unit](
      disableHookBusinessLogic(war.id, 3)
    )
    checkSuccessfulAction(per2.id, 10, 3, toTake = false)
    checkSuccess[AE, Unit](
      disableHookBusinessLogic(war.id, 3),
      _ => succeed
    )
    checkError[AE, ServiceUnavailableError, HookActionResponse](
      hookActionBusinessLogic(war.id, per2.id, 1)
    )
    checkSuccess[AE, Unit](
      (1 to 3)
        .map(i => EitherT(enableHookBusinessLogic(war.id, i)))
        .fold(EitherT.fromEither[IO](Right(())))((a, b) => a.flatMap(_ => b))
        .value,
      _ => succeed
    )
    checkSuccessfulAction(per1.id, 0, 4, toTake = false)
  }

  "composition2" should "perform correctly" in {
    checkSuccessfulAction(per1.id, 2, 2, toTake = true)
    checkSuccess[AE, Unit](
      disableHookBusinessLogic(war.id, 3),
      _ => succeed
    )
    checkSuccessfulAction(per2.id, 3, 4, toTake = true)
    checkSuccessfulAction(per1.id, 4, 2, toTake = false)
    checkSuccessfulAction(per2.id, 2, 4, toTake = false)
    checkSuccess[AE, Unit](
      enableHookBusinessLogic(war.id, 3),
      _ => succeed
    )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    orgDelBusinessLogic(org).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(_)    => ()
    }
  }

}
