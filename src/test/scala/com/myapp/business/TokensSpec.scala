package com.myapp.business

import cats.effect.IO
import com.myapp.error.ApiError._
import com.myapp.SpecCommons._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.implicitConversions

class TokensSpec extends AnyFlatSpec with Matchers {

  val tokens: Tokens[IO] = Tokens[IO]
  import tokens._

  implicit def toIO[T](a: T): IO[T] = IO(a)

  private def checkTokenApi(
    name: String,
    gen1: String => String,
    gen2: String => String,
    check: String => Either[AE, Unit]
  ): Assertion = {
    val token: String = gen1(name)
    checkSuccess[AE, Unit](check(token), _ => succeed)
    checkError[AE, UnauthorizedError, Unit](check(invalid))
    checkError[AE, ForbiddenError, Unit](check(gen2(name)))
  }

  "ownerBundle" should "generate owner token and check it correctly" in {
    checkTokenApi("test owner", genOwnToken, genPerToken, checkOwner)
  }

  "orgBundle" should "generate org token and check it correctly" in {
    checkTokenApi("test org", genOrgToken, genOwnToken, checkOrg)
  }

  "personBundle" should "generate person token and check it correctly" in {
    checkTokenApi("test person", genPerToken, genWarToken, checkPerson)
  }

  "wardrobeBundle" should "generate wardrobe token and check it correctly" in {
    checkTokenApi("test wardrobe", genWarToken, genPerToken, checkWardrobe)
  }

}
