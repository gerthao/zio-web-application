package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.repositories.{CompanyRepository, CompanyRepositoryLive}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

object CompanyRepositoryDemo extends ZIOAppDefault:
    val program: ZIO[CompanyRepository, Throwable, Unit] =
        for
            repo <- ZIO.service[CompanyRepository]
            _    <- repo.create(Company(-1L, "hello-world", "Hello World", "helloworld.org"))
        yield ()

    override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =
        program.provide(
            CompanyRepositoryLive.layer,
            Quill.Postgres.fromNamingStrategy(SnakeCase),
            Quill.DataSource.fromPrefix("application.db")
        )
