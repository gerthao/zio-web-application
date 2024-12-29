package com.rockthejvm.reviewboard

import zio.*
import zio.http.Server
import sttp.tapir.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions};
import PipeOps.*

object Application extends ZIOAppDefault:
    private val healthEndpoint = endpoint
        .tag("health")
        .name("health")
        .description("health check")
        .get
        .in("health")
        .out(plainBody[String])
        .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

    private val serverProgram = ZioHttpServerOptions.default
        |> ZioHttpInterpreter.apply
        |> (_.toHttp(healthEndpoint))
        |> Server.serve

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
        serverProgram.provide(Server.default)
