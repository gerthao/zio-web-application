package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoints extends BaseEndpoint:
    val healthEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] = baseEndpoint
        .tag("health")
        .name("health")
        .description("health check")
        .get
        .in("health")
        .out(plainBody[String])

    val errorEndpoint: Endpoint[Unit, Unit, Throwable, String, Any] = baseEndpoint
        .tag("health")
        .name("error health")
        .description("health check - should fail")
        .get
        .in("health" / "error")
        .out(plainBody[String])
