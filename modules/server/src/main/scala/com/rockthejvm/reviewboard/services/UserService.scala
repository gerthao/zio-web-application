package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.User
import zio.Task

trait UserService:
    def registerUser(email: String, password: String): Task[User]
    def verifyEmail(email: String, password: String): Task[Boolean]
