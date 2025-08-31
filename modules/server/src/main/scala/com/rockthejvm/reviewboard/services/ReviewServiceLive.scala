package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.{Task, ZIO, ZLayer}

import java.time.Instant

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService:
    override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
        repo.create(
            Review(
                id = -1L,
                companyId = req.companyId,
                userId = userId,
                management = req.management,
                culture = req.culture,
                salary = req.salary,
                benefits = req.benefits,
                wouldRecommend = req.wouldRecommend,
                review = req.review,
                created = Instant.now(),
                updated = Instant.now()
            )
        )

    override def getById(id: Long): Task[Option[Review]] =
        repo.getById(id)

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
        repo.getByCompanyId(companyId)

    override def getByUserId(userId: Long): Task[List[Review]] =
        repo.getByUserId(userId)

object ReviewServiceLive:
    val layer: ZLayer[ReviewRepository, Nothing, ReviewServiceLive] = ZLayer:
        ZIO.service[ReviewRepository].map(repo => new ReviewServiceLive(repo))