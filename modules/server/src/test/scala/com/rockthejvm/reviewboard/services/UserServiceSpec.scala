package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.PipeOps.*
import com.rockthejvm.reviewboard.domain.data.{User, UserId, UserToken}
import com.rockthejvm.reviewboard.repositories.UserRepository
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault:
    val testUser = User(
        1L,
        "user@email.abc",
        "1000:ACF03745C6107D9087D8F58BA4D0F7BED5845931DBFA9F2A:07365D7C2A3FEE2A3A725D72A2EDAFD5E5F92686BEB07E5B"
    )

    val stubRepoLayer: ULayer[UserRepository] = ZLayer.succeed:
        new UserRepository:
            val db = collection.mutable.Map[Long, User](1L -> testUser)

            override def create(user: User): Task[User] = ZIO.succeed:
                (user.id -> user) |> db.addOne
                user

            override def delete(id: Long): Task[User] = ZIO.attempt:
                val user = id |> db
                id |> db.subtractOne
                user

            override def getByEmail(email: String): Task[Option[User]] = ZIO.succeed:
                db.values.find(_.email == email)

            override def getById(id: Long): Task[Option[User]] = ZIO.succeed:
                id |> db.get

            override def update(id: Long, op: User => User): Task[User] = ZIO.attempt:
                val updatedUser = id |> db |> op
                (updatedUser.id -> updatedUser) |> db.addOne
                updatedUser

    val stubJwtLayer: ULayer[JwtService] = ZLayer.succeed:
        new JwtService:
            override def createToken(user: User): Task[UserToken] = ZIO.succeed:
                UserToken(user.email, "BiG ACCESS", Long.MaxValue)

            override def verifyToken(token: String): Task[UserId] = ZIO.succeed:
                UserId(testUser.id, testUser.email)

    val testCreateAndVerifyUser: Spec[UserService, Throwable] = test("create and verify a user"):
        for
            service        <- ZIO.service[UserService]
            registeredUser <- service.registerUser(testUser.email, "password")
            isVerified     <- service.verifyEmail(testUser.email, "password")
        yield assertTrue(
            isVerified,
            testUser.email == registeredUser.email
        )

    val testVerifyCorrectCredentials: Spec[UserService, Throwable] = test("verify correct credentials"):
        for
            service    <- ZIO.service[UserService]
            isVerified <- service.verifyEmail(testUser.email, "password")
        yield assertTrue(
            isVerified,
            testUser.email == testUser.email
        )

    val testInvalidateIncorrectPassword: Spec[UserService, Throwable] = test("invalidate incorrect password"):
        for
            service    <- ZIO.service[UserService]
            isVerified <- service.verifyEmail(testUser.email, "p@ssw0rD")
        yield assertTrue(
            !isVerified
        )

    val testInvalidateNonexistentUser: Spec[UserService, Throwable] = test("invalidate nonexistent user"):
        for
            service    <- ZIO.service[UserService]
            isVerified <- service.verifyEmail("user@qwerty.abc", "password-")
        yield assertTrue(
            !isVerified
        )

    val testUpdatePassword: Spec[UserService, Throwable] = test("update password"):
        for
            service  <- ZIO.service[UserService]
            newUser  <- service.updatePassword(testUser.email, "password", "qwerty")
            oldValue <- service.verifyEmail(testUser.email, "password")
            newValue <- service.verifyEmail(testUser.email, "qwerty")
        yield assertTrue(
            !oldValue,
            newValue
        )

    val testDeleteNonexistentUserFailure: Spec[UserService, User] = test("delete nonexistent user should fail"):
        for
            service <- ZIO.service[UserService]
            error <- service.deleteUser("asdfqwerqwers@asdfawef.ovinmsv", "pqwerqwg").flip
        yield assertTrue(
            error.isInstanceOf[RuntimeException],
        )

    val testDeleteExistentUser: Spec[UserService, Throwable] = test("delete existing user should succeed"):
        for
            service <- ZIO.service[UserService]
            deletedUser <- service.deleteUser(testUser.email, "password")
        yield assertTrue(
            testUser.email == deletedUser.email
        )

    override def spec: Spec[TestEnvironment & Scope, Any] = suite("UserServiceSpec")(
        testCreateAndVerifyUser,
        testVerifyCorrectCredentials,
        testInvalidateIncorrectPassword,
        testInvalidateNonexistentUser,
        testUpdatePassword,
        testDeleteNonexistentUserFailure,
        testDeleteExistentUser
    ).provide(
        UserServiceLive.layer,
        stubRepoLayer,
        stubJwtLayer
    )
