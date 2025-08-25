package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.{Company, Review}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository:
    def create(review: Review): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))

    def getById(id: Long): Task[Option[Review]] = ZIO.fail(new RuntimeException("not implemented"))

    def getByCompanyId(companyId: Long): Task[List[Review]] = ZIO.fail(new RuntimeException("not implemented"))

    def getByUserId(userId: Long): Task[List[Review]] = ZIO.fail(new RuntimeException("not implemented"))

    def update(id: Long, op: Review => Review): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))

    def delete(id: Long): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))


object ReviewRepositoryLive:
    val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, ReviewRepositoryLive] = ZLayer:
        ZIO.service[Quill.Postgres[SnakeCase]].map(ReviewRepositoryLive(_))
