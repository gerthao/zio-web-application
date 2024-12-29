package com.rockthejvm

import zio.{Task, ZIO, ZLayer}

class UserDatabase(connectionPool: ConnectionPool):
    def insert(user: User): Task[Unit] = ZIO.succeed(s"inserted $user")

object UserDatabase:
    val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
        ZLayer.fromFunction(new UserDatabase(_))
