package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.{Configs, JwtConfig}
import com.rockthejvm.reviewboard.domain.data.User
import zio.config.*
import zio.config.magnolia.*
import zio.{Console, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, *}

object JwtServiceDemo extends ZIOAppDefault:
    val program: ZIO[JwtService, Throwable, Unit] =
        for
            service   <- ZIO.service[JwtService]
            userToken <- service.createToken(User(1L, "hello@world.goodbye", "something"))
            _         <- Console.printLine(userToken)
            userId    <- service.verifyToken(userToken.token)
            _         <- Console.printLine(userId.toString)
        yield ()

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = program.provide(
        JwtServiceLive.layer,
        Configs.makeConfigLayer[JwtConfig]("application.jwt")
    )
