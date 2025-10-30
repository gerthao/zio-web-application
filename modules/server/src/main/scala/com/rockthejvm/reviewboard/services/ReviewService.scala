package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import zio.Task

trait ReviewService:
    def create(req: CreateReviewRequest, userId: Long): Task[Review]

    def getByCompanyId(companyId: Long): Task[List[Review]]

    def getById(id: Long): Task[Option[Review]]

    def getByUserId(userId: Long): Task[List[Review]]
