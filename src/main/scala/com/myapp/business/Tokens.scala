package com.myapp.business

import cats.Applicative
import cats.syntax.applicative._
import com.myapp.error.ApiError
import com.myapp.error.ApiError._
import com.myapp.types.AuthTokenTypes._
import pdi.jwt._
import scala.util.{Failure, Success}
import io.circe.generic.auto._
import io.circe.parser.decode

class Tokens[F[_]: Applicative] {

  private val secret: String   = "super_secret"
  private val owner: String    = "owner"
  private val org: String      = "organization"
  private val person: String   = "person"
  private val wardrobe: String = "wardrobe"

  private def generateToken(role: String, name: String): F[String] =
    Jwt
      .encode(
        JwtClaim(
          s"""{"role":"$role","name":"$name","datetime":"${System.currentTimeMillis()}"}"""
        ),
        secret,
        JwtAlgorithm.HS256
      )
      .pure

  def generateOwnerToken(name: String): F[OwnerToken] =
    generateToken(owner, name)

  def generateOrgToken(name: String): F[OrgToken] =
    generateToken(org, name)

  def generatePersonToken(name: String): F[PersonToken] =
    generateToken(person, name)

  def generateWardrobeToken(name: String): F[WardrobeToken] =
    generateToken(wardrobe, name)

  private def parseRole(
    role: String,
    payload: String
  ): Either[ApiError, Unit] = {

    case class TokenPayload(role: String, name: String, datetime: String)

    decode[TokenPayload](payload) match {
      case Right(TokenPayload(r, _, _)) if r == role => Right(())
      case Right(_) => Left(ForbiddenError(s"this is not $role token"))
      case Left(_)  => Left(UnauthorizedError("token parsing failed"))
    }
  }

  private def checkRole(role: String, token: String): Either[ApiError, Unit] =
    Jwt.decode(token, secret, Seq(JwtAlgorithm.HS256)) match {
      case Success(decoded) => parseRole(role, decoded.content)
      case Failure(_)       => Left(UnauthorizedError("token decoding failed"))
    }

  def checkOwner(token: OwnerToken): Either[ApiError, Unit] =
    checkRole(owner, token)

  def checkOrg(token: OrgToken): Either[ApiError, Unit] =
    checkRole(org, token)

  def checkPerson(token: PersonToken): Either[ApiError, Unit] =
    checkRole(person, token)

  def checkWardrobe(token: WardrobeToken): Either[ApiError, Unit] =
    checkRole(wardrobe, token)

}

object Tokens {
  def apply[F[_]: Applicative]: Tokens[F] = new Tokens[F]
}
