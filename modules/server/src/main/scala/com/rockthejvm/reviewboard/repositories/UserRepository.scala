package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.User
import zio.*

trait UserRepository:
    def create(user: User): Task[User]

    def delete(id: Long): Task[User]

    def getByEmail(email: String): Task[Option[User]]

    def getById(id: Long): Task[Option[User]]

    def update(id: Long, op: User => User): Task[User]
