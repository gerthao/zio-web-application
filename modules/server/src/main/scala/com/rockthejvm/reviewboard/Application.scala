package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.PipeOps.*
import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.{
    CompanyRepositoryLive,
    Repository,
    ReviewRepositoryLive
}
import com.rockthejvm.reviewboard.services.{CompanyServiceLive, ReviewServiceLive}
import io.getquill.SnakeCase
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
            // services
            CompanyServiceLive.layer,
            ReviewServiceLive.layer,
            // repositories
            CompanyRepositoryLive.layer,
            ReviewRepositoryLive.layer,
            // other requirements
            Repository.dataLayer
        )
