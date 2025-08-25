package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import zio.*

trait ReviewRepository:
    def create(review: Review): Task[Review]
    def getById(id: Long): Task[Option[Review]]
    def getByCompanyId(companyId: Long): Task[List[Review]]
    def getByUserId(userId: Long): Task[List[Review]]
    def update(id: Long, op: Review => Review): Task[Review]
    def delete(id: Long): Task[Review]
