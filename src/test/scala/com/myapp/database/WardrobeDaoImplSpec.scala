package com.myapp.database

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import com.myapp.business.Tokens
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.SpecCommons._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._
import doobie.hikari.HikariTransactor
import java.time.LocalDateTime
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

@DoNotDiscover
class WardrobeDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val txr: Resource[IO, HikariTransactor[IO]] =
    new CreateTransactorImpl[IO].createTransactor

  val orgDao: OrgDao[IO]           = new OrgDaoImpl[IO](txr)
  val wardrobeDao: WardrobeDao[IO] = new WardrobeDaoImpl[IO](txr)
  val tokens: Tokens[IO]           = Tokens[IO]

  var id: WardrobeId              = 0
  var orgId: OrgId                = 0
  val name: String                = "test wardrobe"
  val hooksCount: Int             = 5
  val token: WardrobeToken        = genWarToken(name)
  var registeredAt: LocalDateTime = LocalDateTime.now()

  import orgDao._
  import wardrobeDao._

  override def beforeAll(): Unit = {
    super.beforeAll()
    (for {
      token  <- tokens.generateOrgToken("abc")
      result <- insertOrg("abc", "abc", token)
    } yield result).unsafeRunSync() match {
      case Left(error)  => fail(s"unexpected error: $error")
      case Right(value) => orgId = value.id
    }
  }

  "insertWardrobe" should "correctly insert new wardrobe to db" in {
    insertWardrobe(orgId, name, hooksCount, token)
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
      insertWardrobe(orgId, name, hooksCount, token)
    )
  }

  it should "fail with error if hooksCount is non-positive" in {
    checkError[DE, DE, WardrobeRegInfo](
      insertWardrobe(orgId, name, -2, "some other token")
    )
  }

  "findWardrobe" should "find wardrobe with given token" in {
    checkSuccess[DE, WardrobeInfo](
      findWardrobe(token),
      _ shouldEqual WardrobeInfo(id, orgId, name, hooksCount, registeredAt)
    )
  }

  it should "fail with error if wardrobe with given token not found" in {
    checkError[DE, NotFoundErrorD, WardrobeInfo](findWardrobe(invalid))
  }

  "deleteWardrobe" should "delete wardrobe with given id" in {
    checkSuccess[DE, Unit](deleteWardrobe(id), _ => succeed)
  }

  it should "return error when operation repeats" in {
    checkError[DE, DeleteErrorD, Unit](deleteWardrobe(id))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    deleteOrg(orgId).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(_)    => ()
    }
  }

}
