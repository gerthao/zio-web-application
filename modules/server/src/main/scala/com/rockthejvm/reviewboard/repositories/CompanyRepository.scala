package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import zio.*

trait CompanyRepository:
    def create(company: Company): Task[Company]

    def delete(id: Long): Task[Company]

    def getAll: Task[List[Company]]

    def getById(id: Long): Task[Option[Company]]

    def getBySlug(slug: String): Task[Option[Company]]

    def update(id: Long, op: Company => Company): Task[Company]
