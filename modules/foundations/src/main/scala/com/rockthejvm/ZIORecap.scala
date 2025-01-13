package com.rockthejvm

import zio.*

import java.io.IOException
import scala.io.StdIn

object ZIORecap extends ZIOAppDefault:
    /**
     * ZIO = data structure describing arbitrary computations (including side effects) "effects" =
     * computations as values
     */

    // basics
    val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
    // fail
    val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("Something went wrong")
    // suspension/delay
    val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)

    // map/flatmap
    val improvedMOL: ZIO[Any, Nothing, Int] = meaningOfLife.map(_ * 2)
    val printingMOL: ZIO[Any, Nothing, Unit] =
        meaningOfLife.flatMap(mol => ZIO.succeed(println(mol)))

    val smallProgram: ZIO[Any, IOException, Unit] = for
        _    <- Console.printLine("What's your name?")
        name <- ZIO.succeed(StdIn.readLine())
        _    <- Console.printLine(s"Welcome to ZIO, $name")
    yield ()

    // error handling
    val anAttempt: ZIO[Any, Throwable, Int] = ZIO.attempt:
        // some expression that can throw
        println("Trying something")
        val string: String = null
        string.length

    // catching errors "effectfully"
    val catchError: ZIO[Any, Nothing, Int | String] =
        anAttempt.catchAll(e => ZIO.succeed("Returning some different value"))
    val catchSelective: ZIO[Any, Throwable, Int | String] = anAttempt.catchSome:
        case e: RuntimeException =>
            ZIO.succeed(s"Ignoring runtime exception: $e")
        case _ => ZIO.succeed("Ignoring everything else")
    end catchSelective

    // fibers
    val delayedValue: ZIO[Any, Nothing, Int] =
        ZIO.sleep(1.second) *> Random.nextIntBetween(0, 100)
    val aPair: ZIO[Any, Nothing, (Int, Int)] = for
        a <- delayedValue
        b <- delayedValue
    yield (a, b) // this takes 2 seconds

    val aPairPar: ZIO[Any, Nothing, (Int, Int)] = for
        fiberA <- delayedValue.fork
        fiberB <- delayedValue.fork
        a      <- fiberA.join
        b      <- fiberB.join
    yield (a, b) // this takes 1 second

    val interruptedFiber = for
        fiber <- delayedValue
            .map(println)
            .onInterrupt(ZIO.succeed(println("I'm interrupted!")))
            .fork
        _ <- ZIO.sleep(500.millis) *> ZIO.succeed(
            println("cancelling fiber")
        ) *> fiber.interrupt
        _ <- fiber.join
    yield ()

    val ignoredInterruption = for
        fiber <- ZIO
            .uninterruptible(
                delayedValue
                    .map(println)
                    .onInterrupt(ZIO.succeed(println("I'm interrupted!")))
            )
            .fork
        _ <- ZIO.sleep(500.millis) *> ZIO.succeed(
            println("cancelling fiber")
        ) *> fiber.interrupt
        _ <- fiber.join
    yield ()

    // many APIs on top of fibers
    val aPairParV2 = delayedValue zipPar delayedValue
    val randomX10 =
        ZIO.collectAllPar((1 to 10).map(_ => delayedValue)) // "traverse"

    // dependencies
    def subscribe(user: User): ZIO[UserSubscription, Throwable, Unit] =
        for
            sub <- ZIO.service[UserSubscription]
            _   <- sub.subscribeUser(user)
        yield ()
    end subscribe

    val program: ZIO[UserSubscription, Throwable, Unit] =
        for
            _ <- subscribe(User("Homer", "homer@thompson.com"))
            _ <- subscribe(User("Milhouse", "milhouse@thrillho.com"))
        yield ()

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = program.provide(
        ConnectionPool.live(10), // build me a ConnectionPool
        UserDatabase.live,       // build me a UserDatabase, using a ConnectionPool
        EmailService.live,       // build me a EmailService
        UserSubscription.live // build me a UserSubscription, using the EmailService and UserDatabase
    )

end ZIORecap
