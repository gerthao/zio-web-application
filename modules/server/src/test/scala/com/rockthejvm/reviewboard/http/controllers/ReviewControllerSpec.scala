package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.syntax.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

object ReviewControllerSpec extends ZIOSpecDefault:
    private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

    val goodReview = Review(
        id = 1L,
        companyId = 1L,
        userId = 1L,
        management = 5,
        culture = 5,
        salary = 5,
        benefits = 5,
        wouldRecommend = 10,
        review = "all good",
        created = java.time.Instant.now(),
        updated = java.time.Instant.now()
    )

    private val serviceStub = new ReviewService:
        override def create(req: CreateReviewRequest, userId: Long): Task[Review] = ZIO.succeed:
            goodReview

        override def getById(id: Long): Task[Option[Review]] = ZIO.succeed:
            if id == 1 then Some(goodReview) else None

        override def getByCompanyId(companyId: Long): Task[List[Review]] = ZIO.succeed:
            if companyId == 1 then List(goodReview) else List.empty

        override def getByUserId(userId: Long): Task[List[Review]] = ZIO.succeed:
            if userId == 1 then List(goodReview) else List.empty

    private def backendStubZIO(endpointFunction: ReviewController => ServerEndpoint[Any, Task]) =
        for
            controller <- ReviewController.makeZIO
            backendStub <- ZIO.succeed:
                TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                    .whenServerEndpointRunLogic(endpointFunction(controller))
                    .backend()
        yield backendStub

    private val testPostReview = test("post review"):
        val program = for
            backendStub <- backendStubZIO(_.create)
            response <- basicRequest
                .post(uri"/reviews")
                .body(
                    CreateReviewRequest(
                        companyId = 1L,
                        management = 5,
                        culture = 5,
                        salary = 5,
                        benefits = 5,
                        wouldRecommend = 10,
                        review = "all good"
                    ).toJson
                )
                .send(backendStub)
        yield response.body

        program.assert(
            _.toOption.flatMap(_.fromJson[Review].toOption).contains(goodReview)
        )

    private val testGetById = test("get by id"):
        for
            backendStub <- backendStubZIO(_.getById)
            response <- basicRequest
                .get(uri"/reviews/1")
                .send(backendStub)
            responseNotFound <- basicRequest
                .get(uri"/reviews/999")
                .send(backendStub)
        yield assertTrue(
            response.body.toOption.flatMap(_.fromJson[Review].toOption).contains(goodReview),
            responseNotFound.body.toOption.flatMap(_.fromJson[Review].toOption).isEmpty
        )

    private val testGetByCompanyId = test("get by company id"):
        for
            backendStub <- backendStubZIO(_.getByCompanyId)
            response <- basicRequest
                .get(uri"/reviews/company/1")
                .send(backendStub)
            responseNotFound <- basicRequest
                .get(uri"/reviews/company/999")
                .send(backendStub)
        yield assertTrue(
            response.body.toOption
                .flatMap(_.fromJson[List[Review]].toOption)
                .contains(List(goodReview)),
            responseNotFound.body.toOption
                .flatMap(_.fromJson[List[Review]].toOption)
                .contains(List.empty)
        )

    override def spec: Spec[TestEnvironment & Scope, Any] =
        suite("ReviewControllerSpec")(
            testPostReview,
            testGetById,
            testGetByCompanyId
        ).provide(ZLayer.succeed(serviceStub))
