package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.PipeOps.*
import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.{CompanyRepositoryLive, Repository}
import com.rockthejvm.reviewboard.services.{CompanyServiceDummy, CompanyServiceLive}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server

object Application extends ZIOAppDefault:
    private val serverProgram =
        for
            endpoints <- HttpApi.endpointsZIO
            _ <- ZioHttpServerOptions.default
                |> ZioHttpInterpreter.apply
                |> (_.toHttp(endpoints))
                |> Server.serve
        yield ()

    override def run: ZIO[Any & ZIOAppArgs & Scope, Throwable, Unit] =
        serverProgram.provide(
          Server.default,
          CompanyServiceLive.layer,
          CompanyRepositoryLive.layer,
          Repository.dataLayer
        )
