package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoints
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio.{Task, ZIO}

class HealthController private extends BaseController with HealthEndpoints:
    val health: ServerEndpoint[Any, Task] =
        healthEndpoint.serverLogicSuccess[Task]: _ =>
            ZIO.succeed("All good!")

    override val routes: List[ServerEndpoint[Any, Task]] = List(health)

object HealthController:
    val makeZIO: ZIO[Any, Nothing, HealthController] =
        ZIO.succeed(new HealthController)
