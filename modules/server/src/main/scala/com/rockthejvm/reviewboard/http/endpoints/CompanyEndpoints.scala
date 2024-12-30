package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

trait CompanyEndpoints:
    protected val createEndpoint
        : Endpoint[Unit, CreateCompanyRequest, Unit, Company, Any] =
        endpoint
            .tag("companies")
            .name("create")
            .description("create a listing for a company")
            .in("companies")
            .post
            .in(jsonBody[CreateCompanyRequest])
            .out(jsonBody[Company])

    protected val getAllEndpoint: Endpoint[Unit, Unit, Unit, List[Company], Any] =
        endpoint
            .tag("companies")
            .name("getAll")
            .description("get all company listings")
            .in("companies")
            .get
            .out(jsonBody[List[Company]])
        
    protected val getByIdEndpoint: Endpoint[Unit, String, Unit, Option[Company], Any] =
        endpoint
            .tag("companies")
            .name("getById")
            .description("get company by its id (or maybe by slug?)")
            .in("companies" / path[String]("id"))
            .get
            .out(jsonBody[Option[Company]])