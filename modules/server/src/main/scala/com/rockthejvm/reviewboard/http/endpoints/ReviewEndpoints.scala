package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait ReviewEndpoints extends BaseEndpoint:
    val createEndpoint: Endpoint[Unit, CreateReviewRequest, Throwable, Review, Any] = baseEndpoint
        .tag("reviews")
        .name("create")
        .description("add a review for a company")
        .in("reviews")
        .post
        .in(jsonBody[CreateReviewRequest])
        .out(jsonBody[Review])

    val getByIdEndpoint: Endpoint[Unit, Long, Throwable, Option[Review], Any] = baseEndpoint
        .tag("reviews")
        .name("getById")
        .description("get review by its id")
        .in("reviews" / path[Long]("id"))
        .get
        .out(jsonBody[Option[Review]])

    val getByCompanyIdEndpoint: Endpoint[Unit, Long, Throwable, List[Review], Any] = baseEndpoint
        .tag("reviews")
        .name("getByCompanyId")
        .description("get reviews by company id")
        .in("reviews" / "company" / path[Long]("companyId"))
        .get
        .out(jsonBody[List[Review]])
