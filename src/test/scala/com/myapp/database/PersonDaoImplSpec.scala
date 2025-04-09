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

class PersonDaoImplSpec
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with BeforeAndAfterAll {

  val env = new DataAccessEnvironment

  var od: OrgDao[IO]     = null
  var pd: PersonDao[IO]  = null
  val tokens: Tokens[IO] = Tokens[IO]

  var id: PersonId                = 0
  var orgId: OrgId                = 0
  val name: String                = "test person"
  val token: PersonToken          = genPerToken(name)
  var registeredAt: LocalDateTime = LocalDateTime.now()

  override def beforeAll(): Unit = {
    super.beforeAll()
    env.start()
    od = env.makeOrgDao
    pd = env.makePersonDao
    (for {
      token  <- tokens.generateOrgToken("abc")
      result <- od.insertOrg("abc", "abc", token)
    } yield result).unsafeRunSync() match {
      case Left(error)  => fail(s"unexpected error: $error")
      case Right(value) => orgId = value.id
    }
  }

  "insertPerson" should "correctly insert new person to db" in {
    pd.insertPerson(orgId, name, token).unsafeRunSync() match {
      case Left(error) => fail(s"unexpected error: $error")
      case Right(value) =>
        id = value.id
        registeredAt = value.registeredAt
        succeed
    }
  }

  it should "fail with error if token conflict occurred" in {
    checkError[DE, DE, PersonRegInfo](pd.insertPerson(orgId, name, token))
  }

  "findPerson" should "find person with given token" in {
    checkSuccess[DE, PersonInfo](
      pd.findPerson(token),
      _ shouldEqual PersonInfo(id, orgId, name, registeredAt)
    )
  }

  it should "fail with error if person with given token not found" in {
    checkError[DE, NotFoundErrorD, PersonInfo](pd.findPerson(invalid))
  }

  "deletePerson" should "delete person with given id" in {
    checkSuccess[DE, Unit](pd.deletePerson(id), _ => succeed)
  }

  it should "return error when operation repeats" in {
    checkError[DE, DeleteErrorD, Unit](pd.deletePerson(id))
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
