package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.User
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

class UserRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepository:
    import quill.*

    override def create(user: User): Task[User] = run:
        query[User]
            .insertValue(lift(user))
            .returning(u => u)

    override def delete(id: Long): Task[User] = run:
        query[User].filter(_.id == lift(id)).delete.returning(r => r)

    override def getByEmail(email: String): Task[Option[User]] =
        run:
            query[User].filter(_.email == lift(email))
        .map(_.headOption)

    override def update(id: Long, op: User => User): Task[User] =
        for
            current <- getById(id).someOrFail:
                new RuntimeException(s"Cannot update user: id $id not found")
            updated <- run:
                query[User]
                    .filter(_.id == lift(id))
                    .updateValue(lift(op(current)))
                    .returning(r => r)
        yield updated

    override def getById(id: Long): Task[Option[User]] =
        run:
            query[User].filter(_.id == lift(id))
        .map(_.headOption)

    inline given userInsertMeta: InsertMeta[User] =
        insertMeta[User](_.id, _.hashedPassword)

    inline given userSchema: SchemaMeta[User] = schemaMeta[User]("users")

    inline given userUpdateMeta: UpdateMeta[User] =
        updateMeta[User](_.id, _.hashedPassword)

object UserRepositoryLive:
    val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserRepositoryLive] = ZLayer:
        ZIO.service[Quill.Postgres[SnakeCase]].map(UserRepositoryLive(_))
