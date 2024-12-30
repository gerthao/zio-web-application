package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.PipeOps.*
import com.rockthejvm.reviewboard.http.HttpApi
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

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
        serverProgram.provide(Server.default)
