package com.myapp

import cats.effect.{IO, Ref}
import cats.effect.unsafe.implicits.global
import com.myapp.business.Tokens
import com.myapp.error._
import com.myapp.types.AuthTokenTypes._
import org.scalatest.Assertion
import org.scalatest.Assertions.fail
import org.scalatest.matchers.should.Matchers._
import scala.reflect.ClassTag

object SpecCommons {

  type AE = ApiError
  type DE = DatabaseError

  val invalid: String = "just invalid entity"

  def checkSuccess[E, R](
    io: IO[Either[E, R]],
    p: R => Assertion
  ): Assertion =
    io.unsafeRunSync() match {
      case Left(error)  => fail(s"unexpected error: $error")
      case Right(value) => p(value)
    }

  def checkError[E, E1 <: E: ClassTag, R](
    io: IO[Either[E, R]]
  ): Assertion =
    io.unsafeRunSync() match {
      case Left(error)  => error shouldBe a[E1]
      case Right(value) => fail(s"expected error but found: $value")
    }

  def makeRef[A, B]: Ref[IO, Map[A, B]] =
    Ref.of[IO, Map[A, B]](Map.empty).unsafeRunSync()

  def genOwnToken(name: String): OwnerToken =
    Tokens[IO].generateOwnerToken(name).unsafeRunSync()

  def genOrgToken(name: String): OrgToken =
    Tokens[IO].generateOrgToken(name).unsafeRunSync()

  def genPerToken(name: String): PersonToken =
    Tokens[IO].generatePersonToken(name).unsafeRunSync()

  def genWarToken(name: String): WardrobeToken =
    Tokens[IO].generateWardrobeToken(name).unsafeRunSync()

}
