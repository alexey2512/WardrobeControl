package com.myapp.mock

import cats.Monad
import cats.effect.Ref
import cats.syntax.functor._
import com.myapp.database.PersonDao
import com.myapp.error.DatabaseError
import com.myapp.error.DatabaseError._
import com.myapp.model.database._
import com.myapp.types.AuthTokenTypes._
import com.myapp.types.IdTypes._

import java.time.LocalDateTime

class PersonDaoMock[F[_]: Monad](
  ref: Ref[F, Map[PersonId, Person]]
) extends PersonDao[F] {

  override def insertPerson(
    orgId: OrgId,
    name: String,
    token: PersonToken
  ): F[Either[DatabaseError, PersonRegInfo]] =
    ref.modify { state =>
      val nextId: PersonId       = state.keys.maxOption.getOrElse(-1) + 1
      val current: LocalDateTime = LocalDateTime.now()
      val result: Either[DatabaseError, PersonRegInfo] =
        state.values.find(_.token == token) match {
          case Some(_) => Left(UnknownErrorD("token conflict"))
          case None    => Right(PersonRegInfo(nextId, current))
        }
      (state + (nextId -> Person(orgId, name, token, current)), result)
    }

  override def findPerson(
    token: PersonToken
  ): F[Either[DatabaseError, PersonInfo]] =
    ref.get.map(
      _.find { case (_, person) => person.token == token } match {
        case Some((id, person)) =>
          Right(PersonInfo(id, person.orgId, person.name, person.registeredAt))
        case None => Left(NotFoundErrorD("no such person"))
      }
    )

  override def deletePerson(id: PersonId): F[Either[DatabaseError, Unit]] =
    ref.modify { state =>
      val result: Either[DatabaseError, Unit] =
        state.keys.find(_ == id) match {
          case Some(_) => Right(())
          case None    => Left(DeleteErrorD("failed to delete organization"))
        }
      (state.removed(id), result)
    }

  override def countInOrg(orgId: OrgId): F[Either[DatabaseError, Int]] =
    ref.get.map(state => Right(state.values.count(_.orgId == orgId)))

  def deleteAllInOrg(orgId: OrgId): F[Either[DatabaseError, Unit]] =
    ref.modify(state =>
      (state.filterNot { case (_, person) => person.orgId == orgId }, Right(()))
    )

}
