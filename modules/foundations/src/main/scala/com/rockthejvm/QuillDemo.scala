package com.rockthejvm

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.{Scope, Task, ZIO, ZIOAppArgs}

object QuillDemo extends zio.ZIOAppDefault:
    val program: ZIO[JobRepository[Task], Throwable, Unit] =
        for
            repo <- ZIO.service[JobRepository[Task]]
            _ <- repo.create(
              Job(-1, "Software engineer", "rockthejvm.com", "Rock the JVM")
            )
            _ <- repo.create(
              Job(-1, "Instructor", "rockthejvm.com", "Rock the JVM")
            )
        yield ()

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = program.provide(
      JobRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase), // quill instance
      Quill.DataSource.fromPrefix(
        "mydbconf"
      ) // reads the config section in application.conf and spins up a datasource
    )
