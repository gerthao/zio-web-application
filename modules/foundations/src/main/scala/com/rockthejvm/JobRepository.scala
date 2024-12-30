package com.rockthejvm

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

trait JobRepository[M[_]]:
    def create(job: Job): M[Job]
    def update(id: Long, op: Job => Job): M[Job]
    def delete(id: Long): M[Job]
    def getById(id: Long): M[Option[Job]]
    def get: M[List[Job]]

object JobRepositoryLive:
    val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, JobRepositoryLive] =
        ZLayer:
            ZIO.service[Quill.Postgres[SnakeCase]]
                .map(quill => JobRepositoryLive(quill))

class JobRepositoryLive(quill: Quill.Postgres[SnakeCase])
    extends JobRepository[Task]:
    // step 1
    import quill.*

    // step 2 - schemas for create, update, ...
    inline given schema: SchemaMeta[Job] =
        schemaMeta[Job]("jobs") // specify the table name
    inline given insMeta: InsertMeta[Job] =
        insertMeta[Job](_.id) // columns to be excluded
    inline given upMeta: UpdateMeta[Job] =
        updateMeta[Job](_.id) // columns to be excluded

    override def create(job: Job): Task[Job] =
        run:
            query[Job]
                .insertValue(lift(job))
                .returning(j => j)

    override def update(id: Long, op: Job => Job): Task[Job] =
        for
            current <- getById(id).someOrFail(
              new RuntimeException(
                s"Could not update: missing key $id"
              )
            )
            updated <- run:
                query[Job]
                    .filter(_.id == lift(id))
                    .updateValue(lift(op(current)))
                    .returning(j => j)
        yield updated

    override def delete(id: Long): Task[Job] =
        run:
            query[Job]
                .filter(_.id == lift(id))
                .delete
                .returning(j => j)

    override def getById(id: Long): Task[Option[Job]] =
        run:
            query[Job]
                .filter(_.id == lift(id))
        .map(_.headOption)

    override def get: Task[List[Job]] = run(query[Job])
