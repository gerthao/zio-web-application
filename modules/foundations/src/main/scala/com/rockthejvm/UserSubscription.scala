package com.rockthejvm

import zio.{Task, ZIO, ZLayer}

class UserSubscription(
    emailService: EmailService,
    userDatabase: UserDatabase
):
    def subscribeUser(user: User): Task[Unit] =
        ZIO.succeed(s"subscribed $user") *> emailService.email(user)

object UserSubscription:
    val live: ZLayer[EmailService & UserDatabase, Nothing, UserSubscription] =
        ZLayer.fromFunction((e: EmailService, u: UserDatabase) => new UserSubscription(e, u))