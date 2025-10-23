package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository:
    import quill.*

    inline given reviewSchema: SchemaMeta[Review] = schemaMeta[Review]("reviews")

    inline given reviewInsertMeta: InsertMeta[Review] =
        insertMeta[Review](_.id, _.created, _.updated)

    inline given reviewUpdateMeta: UpdateMeta[Review] =
        updateMeta[Review](_.id, _.companyId, _.userId, _.created)

    def create(review: Review): Task[Review] =
        run:
            query[Review].insertValue(lift(review)).returning(r => r)

    def getByCompanyId(companyId: Long): Task[List[Review]] =
        run:
            query[Review].filter(_.companyId == lift(companyId))

    def getByUserId(userId: Long): Task[List[Review]] =
        run:
            query[Review].filter(_.userId == lift(userId))

    def update(id: Long, op: Review => Review): Task[Review] =
        for
            current <- getById(id).someOrFail:
                new RuntimeException(s"Cannot update review: id $id not found")
            updated <- run:
                query[Review]
                    .filter(_.id == lift(id))
                    .updateValue(lift(op(current)))
                    .returning(r => r)
        yield updated

    def getById(id: Long): Task[Option[Review]] =
        run:
            query[Review].filter(_.id == lift(id))
        .map(_.headOption)

    def delete(id: Long): Task[Review] = run:
        query[Review].filter(_.id == lift(id)).delete.returning(r => r)

object ReviewRepositoryLive:
    val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepositoryLive] = ZLayer:
        ZIO.service[Quill.Postgres[SnakeCase]].map(ReviewRepositoryLive(_))
