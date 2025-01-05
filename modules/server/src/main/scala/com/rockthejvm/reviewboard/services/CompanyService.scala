package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import zio.*

/**
 * business logic layer, in between the http layer and db layer
 */
trait CompanyService:
    def create(request: CreateCompanyRequest): Task[Company]
    def getAll: Task[List[Company]]
    def getById(id: Long): Task[Option[Company]]
    def getBySlug(slug: String): Task[Option[Company]]



