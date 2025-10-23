package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.User
import com.rockthejvm.reviewboard.repositories.UserRepository
import zio.{Task, ZIO, ZLayer}

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserServiceLive private (userRepository: UserRepository) extends UserService:
    override def registerUser(email: String, password: String): Task[User] =
        userRepository.create(
            User(
                id = -1L,
                email = email,
                hashedPassword = UserServiceLive.Hasher.generateHash(password)
            )
        )

    override def verifyEmail(email: String, password: String): Task[Boolean] =
        for
            existingUser <- userRepository
                .getByEmail(email)
                .someOrFail:
                    new RuntimeException("Cannot verify user email: $email")
            result <- ZIO.attempt:
                UserServiceLive.Hasher.validateHash(password, existingUser.hashedPassword)
        yield result

object UserServiceLive:
    val layer: ZLayer[UserRepository, Nothing, UserServiceLive] = ZLayer:
        ZIO.service[UserRepository].map(UserServiceLive(_))

    object Hasher:
        private val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA512"
        private val PBKDF2_ITERATIONS: Int   = 1000
        private val SALT_BYTE_SIZE: Int      = 24
        private val HASH_BYTE_SIZE: Int      = 24
        private lazy val secretKeyFactory: SecretKeyFactory =
            SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

        private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean =
            val range = 0 until math.min(a.length, b.length)
            val difference = range.foldLeft(a.length ^ b.length):
                case (acc, i) => acc | (a(i) ^ b(i))

            difference == 0

        private def fromHex(hexString: String): Array[Byte] =
            hexString.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

        // string + salt + nIterations PBKDF2
        def generateHash(string: String): String =
            val rng = new SecureRandom()
            val salt = Array.ofDim[Byte](SALT_BYTE_SIZE)

            rng.nextBytes(salt)

            val hexSalt = toHex(salt)
            val byteArray = pdkdf2(string.toCharArray, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
            val hexByteArray = toHex(byteArray)

            s"$PBKDF2_ITERATIONS:$hexSalt:$hexByteArray"

        private def pdkdf2(
            message: Array[Char],
            salt: Array[Byte],
            iterations: Int,
            nBytes: Int
        ): Array[Byte] =
            val keySpec = PBEKeySpec(message, salt, iterations, nBytes * 8)

            secretKeyFactory
                .generateSecret(keySpec)
                .getEncoded

        private def toHex(array: Array[Byte]): String = array.map(b => f"$b%02X").mkString

        def validateHash(string: String, hash: String): Boolean = hash.split(":") match
            case Array(first, second, third) =>
                val iterations = first.toInt
                val salt = fromHex(second)
                val expectedHash = fromHex(third)
                val actualHash = pdkdf2(string.toCharArray, salt, iterations, HASH_BYTE_SIZE)

                compareBytes(expectedHash, actualHash)
            case _ => false

object UserServiceDemo:
    @main def runPasswordHash(): Unit =
        val password = "password"
        // "1000:ACF03745C6107D9087D8F58BA4D0F7BED5845931DBFA9F2A:07365D7C2A3FEE2A3A725D72A2EDAFD5E5F92686BEB07E5B"
        val hashedPassword = UserServiceLive.Hasher.generateHash(password)
        val isValid = UserServiceLive.Hasher.validateHash(password, hashedPassword)

        println(s"Password: $password")
        println(s"Generated Hash: $hashedPassword")
        println(s"Validate Hash: $isValid")