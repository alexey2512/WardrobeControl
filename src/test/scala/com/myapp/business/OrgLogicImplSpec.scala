package com.myapp.business

import cats.effect.testing.scalatest.AsyncIOSpec
import com.myapp.error.ApiError._
import com.myapp.model.api._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class OrgLogicImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with InitBusinessOnMock {

  val owner: OwnerToken    = genOwnToken("")
  var org: OrgInfoResponse = OrgInfoResponse(0, "test org", "*****", null)
  var orgToken: OrgToken   = ""

  val invalidOrg: OrgInfoResponse =
    OrgInfoResponse(-1, invalid, invalid, null)

  import orgLogic._

  "orgRegSecurityLogic" should "accept correct owner token" in {
    checkSuccess[AE, Unit](orgRegSecurityLogic(owner), _ => succeed)
  }

  it should "fail with UnauthorizedError if owner token is invalid" in {
    checkError[AE, UnauthorizedError, Unit](orgRegSecurityLogic(invalid))
  }

  it should "fail with ForbiddenError if token role is not an owner" in {
    checkError[AE, ForbiddenError, Unit](
      orgRegSecurityLogic(genPerToken(invalid))
    )
  }

  "orgRegBusinessLogic" should "register new org correctly" in {
    orgRegBusinessLogic(OrgRegRequest(org.name, org.address))
      .unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        org = org.copy(id = value.id, registeredAt = value.registeredAt)
        orgToken = value.token
        succeed
    }
  }

  it should "fail with BadRequestError if name is empty" in {
    checkError[AE, BadRequestError, OrgRegResponse](
      orgRegBusinessLogic(OrgRegRequest("", org.address))
    )
  }

  "orgInfoSecurityLogic" should "accept correct tokens and find org" in {
    checkSuccess[AE, OrgInfoResponse](
      orgInfoSecurityLogic(owner, orgToken),
      _ shouldEqual org
    )
  }

  it should "fail with UnauthorizedError if org token is invalid" in {
    checkError[AE, UnauthorizedError, OrgInfoResponse](
      orgInfoSecurityLogic(owner, invalid)
    )
  }

  it should "fail with ForbiddenError if token role is not an org" in {
    checkError[AE, ForbiddenError, OrgInfoResponse](
      orgInfoSecurityLogic(owner, genWarToken(invalid))
    )
  }

  "orgInfoBusinessLogic" should "return org info if it exists" in {
    checkSuccess[AE, OrgInfoResponse](
      orgInfoBusinessLogic(org),
      _ shouldEqual org
    )
  }

  it should "return given info with counts = 0 if org doesn't exist" in {
    checkSuccess[AE, OrgInfoResponse](
      orgInfoBusinessLogic(invalidOrg),
      _ shouldEqual invalidOrg
    )
  }

  "orgDelSecurityLogic" should "behave like orgInfoSecurityLogic" in {
    checkSuccess[AE, OrgInfoResponse](
      orgDelSecurityLogic(owner, orgToken),
      _ shouldEqual org
    )
  }

  "orgDelBusinessLogic" should "delete org if it exists" in {
    checkSuccess[AE, OrgInfoResponse](
      orgDelBusinessLogic(org),
      _ shouldEqual org
    )
  }

  it should "fail with InternalServerError if org doesn't exist" in {
    checkError[AE, InternalServerError, OrgInfoResponse](
      orgDelBusinessLogic(invalidOrg)
    )
  }

}
