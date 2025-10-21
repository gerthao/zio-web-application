package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait ReviewEndpoints:
    protected val createEndpoint: Endpoint[Unit, CreateReviewRequest, Unit, Review, Any] =
        endpoint
            .tag("reviews")
            .name("create")
            .description("add a review for a company")
            .in("reviews")
            .post
            .in(jsonBody[CreateReviewRequest])
            .out(jsonBody[Review])

    protected val getByIdEndpoint: Endpoint[Unit, Long, Unit, Option[Review], Any] =
        endpoint
            .tag("reviews")
            .name("getById")
            .description("get review by its id")
            .in("reviews" / path[Long]("id"))
            .get
            .out(jsonBody[Option[Review]])

    protected val getByCompanyIdEndpoint: Endpoint[Unit, Long, Unit, List[Review], Any] =
        endpoint
            .tag("reviews")
            .name("getByCompanyId")
            .description("get reviews by company id")
            .in("reviews" / "company" / path[Long]("companyId"))
            .get
            .out(jsonBody[List[Review]])
