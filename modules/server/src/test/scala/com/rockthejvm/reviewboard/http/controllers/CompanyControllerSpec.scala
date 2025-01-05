package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.generic.auto.*
import sttp.client3.*
import sttp.tapir.server.ServerEndpoint
import zio.*
import zio.test.*
import zio.json.*
import com.rockthejvm.reviewboard.syntax.*

object CompanyControllerSpec extends ZIOSpecDefault:
    private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

    private val dummyCompany = Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

    private val serviceStub = new CompanyService:
        override def create(request: CreateCompanyRequest): Task[Company] = ZIO.succeed:
            dummyCompany

        override def getAll: Task[List[Company]] = ZIO.succeed:
            List(dummyCompany)

        override def getById(id: Long): Task[Option[Company]] = ZIO.succeed:
            Some(dummyCompany).find(_.id == id)

        override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed:
            Some(dummyCompany).find(_.slug == slug)

    private def backendStubZIO(endpointFunction: CompanyController => ServerEndpoint[Any, Task]) =
        for
            controller <- CompanyController.makeZIO
            backendStub <- ZIO.succeed:
                TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                    .whenServerEndpointRunLogic(endpointFunction(controller))
                    .backend()
        yield backendStub

    private val test1 = test("post company"):
        val program = for
            backendStub <- backendStubZIO(_.create)
            response <- basicRequest
                .post(uri"/companies")
                .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)
                .send(backendStub)
        yield response.body

        program.assert: body =>
            body.toOption
                .flatMap(_.fromJson[Company].toOption)
                .contains(Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com"))

    private val test2 = test("get all companies"):
        val program = for
            backendStub <- backendStubZIO(_.getAll)
            response <- basicRequest
                .get(uri"/companies")
                .body(CreateCompanyRequest("Rock the JVM", "rockthejvm. com").toJson)
                .send(backendStub)
        yield response.body

        program.assert: body =>
            body.toOption
                .flatMap(_.fromJson[List[Company]].toOption)
                .contains(List(dummyCompany))

    private val test3 = test("get company by id"):
        val program = for
            backendStub <- backendStubZIO(_.getById)
            response <- basicRequest
                .get(uri"/companies/1")
                .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)
                .send(backendStub)
        yield response.body

        program.assert: body =>
            body.toOption
                .flatMap(_.fromJson[Company].toOption)
                .contains(dummyCompany)

    override def spec: Spec[TestEnvironment & Scope, Any] =
        suite("CompanyControllerSpec")(test1, test2, test3)
            .provide(ZLayer.succeed(serviceStub))
