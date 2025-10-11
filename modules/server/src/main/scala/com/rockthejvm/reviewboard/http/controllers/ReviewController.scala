package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (reviewService: ReviewService) extends BaseController with ReviewEndpoints:
    val create: ServerEndpoint[Any, Task] =
        createEndpoint.serverLogicSuccess: (r: CreateReviewRequest) =>
            reviewService.create(r, -1 /* TODO add user id */)

    val getById: ServerEndpoint[Any, Task] =
        getByIdEndpoint.serverLogicSuccess: (id: Long) =>
            reviewService.getById(id)

    val getByCompanyId: ServerEndpoint[Any, Task] =
        getByCompanyIdEndpoint.serverLogicSuccess: (id: Long) =>
            reviewService.getByCompanyId(id)

    override val routes: List[ServerEndpoint[Any, Task]] =
        List(
            create,
            getById,
            getByCompanyId
        )
        
object ReviewController:
    val makeZIO: ZIO[ReviewService, Nothing, ReviewController] =
        for service <- ZIO.service[ReviewService]
        yield new ReviewController(service)