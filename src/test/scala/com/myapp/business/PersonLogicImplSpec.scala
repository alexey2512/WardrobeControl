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

class PersonLogicImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll
    with InitBusinessOnMock {

  var org: OrgInfoResponse = OrgInfoResponse(0, "test org", "*****", null)
  var orgToken: OrgToken   = ""

  var per: PersonInfoResponse = PersonInfoResponse(0, "test person", null)
  var perToken: PersonToken   = ""

  val invalidPer: PersonInfoResponse = PersonInfoResponse(-1, invalid, null)

  import orgLogic._
  import personLogic._

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

  "personRegSecurityLogic" should "accept org token" in {
    checkSuccess[AE, OrgId](
      personRegSecurityLogic(orgToken),
      _ shouldEqual org.id
    )
  }

  it should "fail with UnauthorizedError if org token is invalid" in {
    checkError[AE, UnauthorizedError, OrgId](personRegSecurityLogic(invalid))
  }

  it should "fail with ForbiddenError if token role is not an org" in {
    checkError[AE, ForbiddenError, OrgId](
      personRegSecurityLogic(genWarToken(invalid))
    )
  }

  "personRegBusinessLogic" should "register new person correctly" in {
    personRegBusinessLogic(org.id, PersonRegRequest(per.name))
      .unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        perToken = value.token
        per = per.copy(id = value.id, registeredAt = value.registeredAt)
        succeed
    }
  }

  it should "fail with BadRequestError if name is empty" in {
    checkError[AE, BadRequestError, PersonRegResponse](
      personRegBusinessLogic(org.id, PersonRegRequest(""))
    )
  }

  "personInfoSecurityLogic" should "accept correct tokens and find person" in {
    checkSuccess[AE, PersonInfoResponse](
      personInfoSecurityLogic(orgToken, perToken),
      _ shouldEqual per
    )
  }

  it should "fail with UnauthorizedError if person token is invalid" in {
    checkError[AE, UnauthorizedError, PersonInfoResponse](
      personInfoSecurityLogic(orgToken, invalid)
    )
  }

  it should "fail with ForbiddenError if token role is not a person" in {
    checkError[AE, ForbiddenError, PersonInfoResponse](
      personInfoSecurityLogic(orgToken, genOrgToken(invalid))
    )
  }

  "personInfoBusinessLogic" should "return person info if it exists" in {
    checkSuccess[AE, PersonInfoResponse](
      personInfoBusinessLogic(per),
      _ shouldEqual per
    )
  }

  it should "return given info also if person doesn't exist" in {
    checkSuccess[AE, PersonInfoResponse](
      personInfoBusinessLogic(invalidPer),
      _ shouldEqual invalidPer
    )
  }

  "personDelSecurityLogic" should "behave like personInfoSecurityLogic" in {
    checkSuccess[AE, PersonInfoResponse](
      personDelSecurityLogic(orgToken, perToken),
      _ shouldEqual per
    )
  }

  "personDelBusinessLogic" should "delete person if it exists" in {
    checkSuccess[AE, PersonInfoResponse](
      personDelBusinessLogic(per),
      _ shouldEqual per
    )
  }

  it should "fail with InternalServerError if person doesn't exist" in {
    checkError[AE, InternalServerError, PersonInfoResponse](
      personDelBusinessLogic(invalidPer)
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
