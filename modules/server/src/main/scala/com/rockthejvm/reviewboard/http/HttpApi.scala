package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.{
    BaseController,
    CompanyController,
    HealthController
}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

object HttpApi:
    private def gatherRoutes(controllers: List[BaseController]) =
        controllers.flatMap(_.routes)

    private def makeControllers: ZIO[Any, Nothing, List[BaseController]] =
        for
            health    <- HealthController.makeZIO
            companies <- CompanyController.makeZIO
        // add new controllers here
        yield List(health, companies)

    val endpointsZIO: ZIO[Any, Nothing, List[ServerEndpoint[Any, Task]]] =
        makeControllers.map(gatherRoutes)