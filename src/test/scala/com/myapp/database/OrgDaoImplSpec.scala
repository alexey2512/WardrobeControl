package com.myapp.database

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import com.myapp.business.Tokens
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import java.time.LocalDateTime
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class OrgDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val env                = new DataAccessEnvironment
  var od: OrgDao[IO]     = null
  val tokens: Tokens[IO] = Tokens[IO]

  var id: OrgId                   = 0
  val name: String                = "test org"
  val address: String             = "*****"
  val token: OrgToken             = genOrgToken(name)
  var registeredAt: LocalDateTime = LocalDateTime.now()

  override def beforeAll(): Unit = {
    super.beforeAll()
    env.start()
    od = env.makeOrgDao
  }

  "insertOrg" should "correctly insert new org to db" in {
    od.insertOrg(name, address, token).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error $error")
      case Right(value) =>
        id = value.id
        registeredAt = value.registeredAt
        succeed
    }
  }

  it should "fail with error if token conflict occurred" in {
    checkError[DE, DE, OrgRegInfo](od.insertOrg(name, address, token))
  }

  "findOrg" should "find org with given token" in {
    checkSuccess[DE, OrgInfo](
      od.findOrg(token),
      _ shouldEqual OrgInfo(id, name, address, registeredAt)
    )
  }

  it should "fail with error if org with given token not found" in {
    checkError[DE, NotFoundErrorD, OrgInfo](od.findOrg(invalid))
  }

  "deleteOrg" should "delete org with given id" in {
    checkSuccess[DE, Unit](od.deleteOrg(id), _ => succeed)
  }

  it should "return error when operation repeats" in {
    checkError[DE, DeleteErrorD, Unit](od.deleteOrg(id))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    env.finish()
  }

}
