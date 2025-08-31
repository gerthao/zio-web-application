package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.{Task, ZIO}

import java.time.Instant

trait ReviewService:
    def create(req: CreateReviewRequest, userId: Long): Task[Review]
    def getById(id: Long): Task[Option[Review]]
    def getByCompanyId(companyId: Long): Task[List[Review]]
    def getByUserId(userId: Long): Task[List[Review]]
