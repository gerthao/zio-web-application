package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.repositories.CompanyRepositorySpec.dataSourceLayer
import zio.test.*
import zio.*
import com.rockthejvm.reviewboard.syntax.*

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/reviews.sql"):
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

    val testCreate: Spec[ReviewRepository, Throwable] = test("create review"):
        for
            repo   <- ZIO.service[ReviewRepository]
            review <- repo.create(goodReview)
        yield assertTrue(
            review.management == goodReview.management,
            review.culture == goodReview.culture,
            review.salary == goodReview.salary,
            review.benefits == goodReview.benefits,
            review.wouldRecommend == goodReview.wouldRecommend
        )

    val testGetByIds: Spec[ReviewRepository, Throwable] =
        test("get review by ids(id, companyId, userId"):
            for
                repo                     <- ZIO.service[ReviewRepository]
                review                   <- repo.create(goodReview)
                fetchedReviewById        <- repo.getById(review.id)
                fetchedReviewByCompanyId <- repo.getById(review.companyId)
                fetchedReviewByUserId    <- repo.getById(review.userId)
            yield assertTrue(
                fetchedReviewById.contains(review)
                    && fetchedReviewByCompanyId.contains(review)
                    && fetchedReviewByUserId.contains(review)
            )

    val testGetAll: Spec[ReviewRepository, Throwable] = test("get all reviews"):
        val program = for
            repo           <- ZIO.service[ReviewRepository]
            review1        <- repo.create(goodReview)
            review2        <- repo.create(badReview)
            companyReviews <- repo.getByCompanyId(1L)
            userReviews    <- repo.getByUserId(1L)
        yield (review1, review2, companyReviews, userReviews)

        program.assert:
            case (review1, review2, companyReviews, userReviews) =>
                companyReviews.toSet == Set(review1, review2)
                && userReviews.toSet == Set(review1, review2)

    val testEdit: Spec[ReviewRepository, Throwable] = test("edit review"):
        for
            repo    <- ZIO.service[ReviewRepository]
            review  <- repo.create(goodReview)
            updated <- repo.update(review.id, _.copy(review = "is okay"))
        yield assertTrue(
            updated.id == review.id,
            updated.companyId == review.companyId,
            updated.userId == review.userId,
            updated.management == review.management,
            updated.culture == review.culture,
            updated.salary == review.salary,
            updated.benefits == review.benefits,
            updated.wouldRecommend == review.wouldRecommend,
            updated.review == "is okay",
            updated.created == review.created,
            updated.updated != review.updated
        )

    val testDelete: Spec[ReviewRepository, Throwable] = test("delete review"):
        for
            repo        <- ZIO.service[ReviewRepository]
            review      <- repo.create(goodReview)
            _           <- repo.delete(review.id)
            maybeReview <- repo.getById(review.id)
        yield assertTrue(
            maybeReview.isEmpty
        )

    override def spec: Spec[TestEnvironment & Scope, Any] =
        suite("ReviewRepositorySpec")(
            testCreate,
            testGetByIds,
            testGetAll,
            testEdit,
            testDelete
        ).provide(
            ReviewRepositoryLive.layer,
            dataSourceLayer,
            Repository.quillLayer,
            Scope.default
        )
end ReviewRepositorySpec
