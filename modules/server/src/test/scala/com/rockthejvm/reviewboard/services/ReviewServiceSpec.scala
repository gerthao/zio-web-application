package com.rockthejvm.reviewboard.services

import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault}

object ReviewServiceSpec extends ZIOSpecDefault:
    override def spec: Spec[TestEnvironment & Scope, Any] = ???
    