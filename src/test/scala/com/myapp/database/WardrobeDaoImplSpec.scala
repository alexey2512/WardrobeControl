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

class WardrobeDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val env                 = new DataAccessEnvironment
  var od: OrgDao[IO]      = null
  var wd: WardrobeDao[IO] = null
  val tokens: Tokens[IO]  = Tokens[IO]

  var id: WardrobeId              = 0
  var orgId: OrgId                = 0
  val name: String                = "test wardrobe"
  val hooksCount: Int             = 5
  val token: WardrobeToken        = genWarToken(name)
  var registeredAt: LocalDateTime = LocalDateTime.now()

  override def beforeAll(): Unit = {
    super.beforeAll()
    env.start()
    od = env.makeOrgDao
    wd = env.makeWardrobeDao
    (for {
      token  <- tokens.generateOrgToken("abc")
      result <- od.insertOrg("abc", "abc", token)
    } yield result).unsafeRunSync() match {
      case Left(error)  => fail(s"unexpected error: $error")
      case Right(value) => orgId = value.id
    }
  }

  "insertWardrobe" should "correctly insert new wardrobe to db" in {
    wd.insertWardrobe(orgId, name, hooksCount, token)
      .unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        id = value.id
        registeredAt = value.registeredAt
        succeed
    }
  }

  it should "fail with error if token conflict occurred" in {
    checkError[DE, DE, WardrobeRegInfo](
      wd.insertWardrobe(orgId, name, hooksCount, token)
    )
  }

  it should "fail with error if hooksCount is non-positive" in {
    checkError[DE, DE, WardrobeRegInfo](
      wd.insertWardrobe(orgId, name, -2, "some other token")
    )
  }

  "findWardrobe" should "find wardrobe with given token" in {
    checkSuccess[DE, WardrobeInfo](
      wd.findWardrobe(token),
      _ shouldEqual WardrobeInfo(id, orgId, name, hooksCount, registeredAt)
    )
  }

  it should "fail with error if wardrobe with given token not found" in {
    checkError[DE, NotFoundErrorD, WardrobeInfo](wd.findWardrobe(invalid))
  }

  "deleteWardrobe" should "delete wardrobe with given id" in {
    checkSuccess[DE, Unit](wd.deleteWardrobe(id), _ => succeed)
  }

  it should "return error when operation repeats" in {
    checkError[DE, DeleteErrorD, Unit](wd.deleteWardrobe(id))
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
