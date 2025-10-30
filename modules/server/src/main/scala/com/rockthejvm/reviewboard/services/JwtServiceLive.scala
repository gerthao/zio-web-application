package com.rockthejvm.reviewboard.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.rockthejvm.reviewboard.config.{Configs, JwtConfig}
import com.rockthejvm.reviewboard.domain.data.{User, UserId, UserToken}
import zio.{Clock, Task, ZIO, ZLayer}

import java.time.Instant

class JwtServiceLive(jwtConfig: JwtConfig, clock: java.time.Clock) extends JwtService:
    private val CLAIM_USERNAME = "username"
    private val ISSUER         = "rockthejvm.com"
    private val algorithm      = Algorithm.HMAC512(jwtConfig.secret)
    private val verifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .asInstanceOf[BaseVerification]
        .build(clock)

    override def createToken(user: User): Task[UserToken] =
        for
            now <- ZIO.attempt:
                clock.instant()
            expiration <- ZIO.succeed:
                now.plusSeconds(jwtConfig.ttl)
            token <- ZIO.attempt:
                JWT.create()
                    .withIssuer(ISSUER)
                    .withIssuedAt(now)
                    .withExpiresAt(expiration)
                    .withSubject(user.id.toString)
                    .withClaim(CLAIM_USERNAME, user.email)
                    .sign(algorithm)
        yield UserToken(user.email, token, expiration.getEpochSecond)

    override def verifyToken(token: String): Task[UserId] =
        for
            decoded <- ZIO.attempt:
                verifier.verify(token)
            userId <- ZIO.attempt:
                UserId(
                    id = decoded.getSubject().toLong,
                    email = decoded.getClaim(CLAIM_USERNAME).asString()
                )
        yield userId

object JwtServiceLive:
    val layer: ZLayer[JwtConfig, Nothing, JwtServiceLive] = ZLayer:
        for
            jwtConfig <- ZIO.service[JwtConfig]
            clock     <- Clock.javaClock
        yield new JwtServiceLive(
            jwtConfig = jwtConfig,
            clock = clock
        )

    val configuredLayer: ZLayer[Any, Throwable, JwtServiceLive] =
        Configs.makeConfigLayer[JwtConfig]("application.jwt") >>> layer
