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
class OrgDaoImplSpec extends AsyncFlatSpec with Matchers with AsyncIOSpec {

  val txr: Resource[IO, HikariTransactor[IO]] =
    new CreateTransactorImpl[IO].createTransactor

  val orgDao: OrgDao[IO] = new OrgDaoImpl[IO](txr)
  val tokens: Tokens[IO] = Tokens[IO]

  var id: OrgId                   = 0
  val name: String                = "test org"
  val address: String             = "*****"
  val token: OrgToken             = genOrgToken(name)
  var registeredAt: LocalDateTime = LocalDateTime.now()

  import orgDao._

  "insertOrg" should "correctly insert new org to db" in {
    insertOrg(name, address, token).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error $error")
      case Right(value) =>
        id = value.id
        registeredAt = value.registeredAt
        succeed
    }
  }

  it should "fail with error if token conflict occurred" in {
    checkError[DE, DE, OrgRegInfo](insertOrg(name, address, token))
  }

  "findOrg" should "find org with given token" in {
    checkSuccess[DE, OrgInfo](
      findOrg(token),
      _ shouldEqual OrgInfo(id, name, address, registeredAt)
    )
  }

  it should "fail with error if org with given token not found" in {
    checkError[DE, NotFoundErrorD, OrgInfo](findOrg(invalid))
  }

  "deleteOrg" should "delete org with given id" in {
    checkSuccess[DE, Unit](deleteOrg(id), _ => succeed)
  }

  it should "return error when operation repeats" in {
    checkError[DE, DeleteErrorD, Unit](deleteOrg(id))
  }

}
