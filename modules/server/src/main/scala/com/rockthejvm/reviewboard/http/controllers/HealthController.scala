package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.errors.HttpError
import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoints
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoints:
    override val routes: List[ServerEndpoint[Any, Task]] = List(health, errorRoute)
    
    val health: ServerEndpoint[Any, Task] =
        healthEndpoint
            .serverLogicSuccess[Task]: _ =>
                ZIO.succeed("All good!")
                
    val errorRoute: ServerEndpoint[Any, Task] =
        errorEndpoint
            .errorOut(statusCode and plainBody[String])
            .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
            .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Something went wrong!")).either)

object HealthController:
    val makeZIO: ZIO[Any, Nothing, HealthController] =
        ZIO.succeed(new HealthController)
