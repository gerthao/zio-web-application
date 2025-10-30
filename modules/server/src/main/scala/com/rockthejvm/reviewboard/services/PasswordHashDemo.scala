package com.rockthejvm.reviewboard.services

@main def runPasswordHash(): Unit =
    val password = "password"
    // "1000:ACF03745C6107D9087D8F58BA4D0F7BED5845931DBFA9F2A:07365D7C2A3FEE2A3A725D72A2EDAFD5E5F92686BEB07E5B"
    val hashedPassword = UserServiceLive.Hasher.generateHash(password)
    val isValid        = UserServiceLive.Hasher.validateHash(password, hashedPassword)

    println(s"Password: $password")
    println(s"Generated Hash: $hashedPassword")
    println(s"Validate Hash: $isValid")
