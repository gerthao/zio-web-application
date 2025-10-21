package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.{
    BaseController,
    CompanyController,
    HealthController,
    ReviewController
}
import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

object HttpApi:
    val endpointsZIO
        : ZIO[ReviewService & CompanyService, Nothing, List[ServerEndpoint[Any, Task]]] =
        makeControllers.map(gatherRoutes)

    private def gatherRoutes(controllers: List[BaseController]) =
        controllers.flatMap(_.routes)

    private def makeControllers
        : ZIO[ReviewService & CompanyService, Nothing, List[BaseController]] =
        for
            health    <- HealthController.makeZIO
            companies <- CompanyController.makeZIO
            reviews   <- ReviewController.makeZIO
        // add new controllers here
        yield List(health, companies, reviews)
