package com.rockthejvm

import zio.{Task, ZIO, ZLayer}

class EmailService:
    def email(user: User): Task[Unit] = ZIO.succeed(s"emailed $user")
    
object EmailService:
    val live: ZLayer[Any, Nothing, EmailService] = ZLayer.succeed(new EmailService)
