package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserId, UserToken}
import zio.*

trait JwtService:
    def createToken(user: User): Task[UserToken]
    def verifyToken(token: String): Task[UserId]
