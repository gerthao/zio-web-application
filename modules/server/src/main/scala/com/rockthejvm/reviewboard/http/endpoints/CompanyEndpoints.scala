package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait CompanyEndpoints extends BaseEndpoint:
    val createEndpoint: Endpoint[Unit, CreateCompanyRequest, Throwable, Company, Any] = baseEndpoint
        .tag("companies")
        .name("create")
        .description("create a listing for a company")
        .in("companies")
        .post
        .in(jsonBody[CreateCompanyRequest])
        .out(jsonBody[Company])

    val getAllEndpoint: Endpoint[Unit, Unit, Throwable, List[Company], Any] = baseEndpoint
        .tag("companies")
        .name("getAll")
        .description("get all company listings")
        .in("companies")
        .get
        .out(jsonBody[List[Company]])

    val getByIdEndpoint: Endpoint[Unit, String, Throwable, Option[Company], Any] = baseEndpoint
        .tag("companies")
        .name("getById")
        .description("get company by its id (or maybe by slug?)")
        .in("companies" / path[String]("id"))
        .get
        .out(jsonBody[Option[Company]])
