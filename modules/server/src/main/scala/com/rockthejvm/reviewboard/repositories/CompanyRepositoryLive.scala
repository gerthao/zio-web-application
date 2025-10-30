package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository:
    import quill.*

    override def create(company: Company): Task[Company] =
        run:
            query[Company]
                .insertValue(lift(company))
                .returning(r => r)

    override def delete(id: Long): Task[Company] =
        run:
            query[Company]
                .filter(_.id == lift(id))
                .delete
                .returning(r => r)

    override def getAll: Task[List[Company]] = run(query[Company])

    override def getBySlug(slug: String): Task[Option[Company]] =
        run:
            query[Company].filter(_.slug == lift(slug))
        .map(_.headOption)

    inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)

    inline given schema: SchemaMeta[Company] = schemaMeta[Company]("companies")

    inline given upMeta: UpdateMeta[Company] = updateMeta[Company](_.id)

    override def update(id: Long, op: Company => Company): Task[Company] =
        for
            current <- getById(id).someOrFail:
                new RuntimeException(s"Cannot update company: id $id not found")
            updated <- run:
                query[Company]
                    .filter(_.id == lift(id))
                    .updateValue(lift(op(current)))
                    .returning(r => r)
        yield updated

    override def getById(id: Long): Task[Option[Company]] =
        run:
            query[Company].filter(_.id == lift(id))
        .map(_.headOption)

object CompanyRepositoryLive:
    val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, CompanyRepositoryLive] = ZLayer:
        ZIO.service[Quill.Postgres[SnakeCase]].map(CompanyRepositoryLive(_))
