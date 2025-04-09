package com.myapp.business

import cats.effect.testing.scalatest.AsyncIOSpec
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class WardrobeLogicImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll
    with BusinessMockInit {

  var org: OrgInfoResponse = OrgInfoResponse(0, "test org", "*****", null)
  var orgToken: OrgToken   = ""

  var war: WardrobeInfoResponse =
    WardrobeInfoResponse(0, "test wardrobe", 5, null)

  var warToken: WardrobeToken = ""

  val invalidWar: WardrobeInfoResponse =
    WardrobeInfoResponse(-1, invalid, 5, null)

  import orgLogic._
  import wardrobeLogic._

  override def beforeAll(): Unit = {
    super.beforeAll()
    orgRegBusinessLogic(OrgRegRequest(org.name, org.address))
      .unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        orgToken = value.token
        org = org.copy(id = value.id, registeredAt = value.registeredAt)
    }
  }

  "wardrobeRegSecurityLogic" should "accept org token" in {
    checkSuccess[AE, OrgId](
      wardrobeRegSecurityLogic(orgToken),
      _ shouldEqual org.id
    )
  }

  it should "fail with UnauthorizedError if org token is invalid" in {
    checkError[AE, UnauthorizedError, OrgId](
      wardrobeRegSecurityLogic(invalid)
    )
  }

  it should "fail with ForbiddenError if token role is not an org" in {
    checkError[AE, ForbiddenError, OrgId](
      wardrobeRegSecurityLogic(genWarToken(invalid))
    )
  }

  "wardrobeRegBusinessLogic" should "register new wardrobe correctly" in {
    wardrobeRegBusinessLogic(
      org.id,
      WardrobeRegRequest(war.name, war.hooksCount)
    )
      .unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        warToken = value.token
        war = war.copy(id = value.id, registeredAt = value.registeredAt)
        succeed
    }
  }

  it should "fail with BadRequestError if name is empty" in {
    checkError[AE, BadRequestError, WardrobeRegResponse](
      wardrobeRegBusinessLogic(org.id, WardrobeRegRequest("", 3))
    )
  }

  "wardrobeInfoSecurityLogic" should "accept correct tokens and find wardrobe" in {
    checkSuccess[AE, WardrobeInfoResponse](
      wardrobeInfoSecurityLogic(orgToken, warToken),
      _ shouldEqual war
    )
  }

  it should "fail with UnauthorizedError if wardrobe token is invalid" in {
    checkError[AE, UnauthorizedError, WardrobeInfoResponse](
      wardrobeInfoSecurityLogic(orgToken, invalid)
    )
  }

  it should "fail with ForbiddenError if token role is not a wardrobe" in {
    checkError[AE, ForbiddenError, WardrobeInfoResponse](
      wardrobeInfoSecurityLogic(orgToken, genPerToken(invalid))
    )
  }

  "wardrobeInfoBusinessLogic" should "return wardrobe info if it exists" in {
    checkSuccess[AE, WardrobeInfoResponse](
      wardrobeInfoBusinessLogic(war),
      _ shouldEqual war.copy(
        enabledHooksCount = war.hooksCount,
        enabledFreeHooksCount = war.hooksCount
      )
    )
  }

  it should "return given info also if wardrobe doesn't exist" in {
    checkSuccess[AE, WardrobeInfoResponse](
      wardrobeInfoBusinessLogic(invalidWar),
      _ shouldEqual invalidWar
    )
  }

  "wardrobeDelSecurityLogic" should "behave like wardrobeInfoSecurityLogic" in {
    checkSuccess[AE, WardrobeInfoResponse](
      wardrobeDelSecurityLogic(orgToken, warToken),
      _ shouldEqual war
    )
  }

  "wardrobeDelBusinessLogic" should "delete wardrobe if it exists" in {
    checkSuccess[AE, WardrobeInfoResponse](
      wardrobeDelBusinessLogic(war),
      _ shouldEqual war.copy(
        enabledHooksCount = war.hooksCount,
        enabledFreeHooksCount = war.hooksCount
      )
    )
  }

  it should "fail with InternalServerError if wardrobe doesn't exist" in {
    checkError[AE, InternalServerError, WardrobeInfoResponse](
      wardrobeDelBusinessLogic(invalidWar)
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
