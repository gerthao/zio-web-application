package com.rockthejvm.reviewboard.repositories

import zio.*
import com.rockthejvm.reviewboard.domain.data.User

trait UserRepository:
    def create(user: User): Task[User]
    def update(id: Long, op: User => User): Task[User]
    def getById(id: Long): Task[Option[User]]
    def getByEmail(email: String): Task[Option[User]]
    def delete(id: Long): Task[User]
