package com.rockthejvm.reviewboard.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.rockthejvm.reviewboard.domain.data.{User, UserId, UserToken}
import zio.{Clock, Task, ZIO, ZLayer}

import java.time.Instant

class JwtServiceLive(clock: java.time.Clock) extends JwtService:
    private val CLAIM_USERNAME = "username"
    private val ISSUER         = "rockthejvm.com"
    private val SECRET         = "secret"
    private val TTL            = 30 * 24 * 3600

    private val algorithm = Algorithm.HMAC512(SECRET)
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
                now.plusSeconds(TTL)
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
    val layer: ZLayer[Any, Nothing, JwtServiceLive] = ZLayer:
        Clock.javaClock.map: clock =>
            new JwtServiceLive(clock)
