package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.JwtConfig
import zio.*
import zio.test.*
import com.rockthejvm.reviewboard.domain.data.*

object JwtServiceSpec extends ZIOSpecDefault:
    override def spec = suite("JWTServiceSpec"):
        test("create and validate token"):
            for
                service <- ZIO.service[JwtService]
                token <- service.createToken:
                    User(1L, "user@email.email", "something")
                userId <- service.verifyToken:
                    token.token
            yield assertTrue:
                userId.id == 1L && userId.email == "user@email.email"
        .provide(
            JwtServiceLive.layer,
            ZLayer.succeed:
                JwtConfig("secret", 3600L)
        )
