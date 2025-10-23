package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.{Scope, Task, ULayer, ZIO, ZLayer}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue, suite}

object ReviewServiceSpec extends ZIOSpecDefault:
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

    val badReview = Review(
        id = 2L,
        companyId = 1L,
        userId = 1L,
        management = 1,
        culture = 1,
        salary = 1,
        benefits = 1,
        wouldRecommend = 1,
        review = "not good",
        created = java.time.Instant.now(),
        updated = java.time.Instant.now()
    )

    val stubRepoLayer: ULayer[ReviewRepository] = ZLayer.succeed:
        new ReviewRepository:
            override def create(review: Review): Task[Review] = ZIO.succeed:
                goodReview

            override def getById(id: Long): Task[Option[Review]] = ZIO.succeed:
                id match
                    case 1 => Some(goodReview)
                    case 2 => Some(badReview)
                    case _ => None

            override def getByCompanyId(companyId: Long): Task[List[Review]] = ZIO.succeed:
                if companyId == 1 then List(goodReview, badReview) else List.empty

            override def getByUserId(userId: Long): Task[List[Review]] = ZIO.succeed:
                if userId == 1 then List(goodReview, badReview) else List.empty

            // not used but needed
            override def update(id: Long, op: Review => Review): Task[Review] =
                getById(id).someOrFail(new RuntimeException(s"id $id not found")).map(op)

            // not used but needed
            override def delete(id: Long): Task[Review] =
                getById(id).someOrFail(new RuntimeException(s"id $id not found"))

    private val testCreate: Spec[ReviewService, Throwable] = test("create"):
        for
            service <- ZIO.service[ReviewService]
            review <- service.create(
                req = CreateReviewRequest(
                    companyId = goodReview.companyId,
                    management = goodReview.management,
                    culture = goodReview.culture,
                    salary = goodReview.salary,
                    benefits = goodReview.benefits,
                    wouldRecommend = goodReview.wouldRecommend,
                    review = goodReview.review
                ),
                userId = 1L
            )
        yield assertTrue(
            review.companyId == goodReview.companyId,
            review.management == goodReview.culture,
            review.culture == goodReview.culture,
            review.salary == goodReview.salary,
            review.benefits == goodReview.benefits,
            review.wouldRecommend == goodReview.wouldRecommend,
            review.review == goodReview.review
        )

    private val testGetById: Spec[ReviewService, Throwable] = test("getById"):
        for
            service        <- ZIO.service[ReviewService]
            review         <- service.getById(1L)
            reviewNotFound <- service.getById(999L)
        yield assertTrue(
            review.contains(goodReview),
            reviewNotFound.isEmpty
        )

    private val testGetByCompanyId: Spec[ReviewService, Throwable] = test("getByCompanyId"):
        for
            service         <- ZIO.service[ReviewService]
            reviews         <- service.getByCompanyId(1L)
            reviewsNotFound <- service.getByCompanyId(999L)
        yield assertTrue(
            reviews.toSet == Set(goodReview, badReview),
            reviewsNotFound.isEmpty
        )

    private val testGetByUserId: Spec[ReviewService, Throwable] = test("getByUserId"):
        for
            service         <- ZIO.service[ReviewService]
            reviews         <- service.getByUserId(1L)
            reviewsNotFound <- service.getByUserId(999L)
        yield assertTrue(
            reviews.toSet == Set(goodReview, badReview),
            reviewsNotFound.isEmpty
        )

    override def spec: Spec[TestEnvironment & Scope, Any] =
        suite("ReviewServiceTestSuite")(
            testCreate,
            testGetById,
            testGetByCompanyId,
            testGetByUserId
        ).provide(
            ReviewServiceLive.layer,
            stubRepoLayer
        )
