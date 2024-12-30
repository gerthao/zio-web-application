package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoints:
    protected val healthEndpoint: Endpoint[Unit, Unit, Unit, String, Any] =
        endpoint
            .tag("health")
            .name("health")
            .description("health check")
            .get
            .in("health")
            .out(plainBody[String])
