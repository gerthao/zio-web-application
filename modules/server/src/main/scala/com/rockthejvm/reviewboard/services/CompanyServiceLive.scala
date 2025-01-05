package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import zio.{Task, ZLayer, ZIO}

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService:
    override def create(request: CreateCompanyRequest): Task[Company] =
        repo.create(request.toCompany(-1L))

    override def getAll: Task[List[Company]] =
        repo.getAll

    override def getById(id: Long): Task[Option[Company]] =
        repo.getById(id)

    override def getBySlug(slug: String): Task[Option[Company]] =
        repo.getBySlug(slug)

object CompanyServiceLive:
    val layer: ZLayer[CompanyRepository, Nothing, CompanyServiceLive] = ZLayer:
        ZIO.service[CompanyRepository].map(CompanyServiceLive(_))