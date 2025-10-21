package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import zio.{Task, ULayer, ZIO, ZLayer}

class CompanyServiceDummy extends CompanyService:
    val db = scala.collection.mutable.Map.empty[Long, Company]

    override def create(request: CreateCompanyRequest): Task[Company] = ZIO.succeed:
        val newId      = db.keys.maxOption.getOrElse(0L) + 1
        val newCompany = request.toCompany(newId)
        db += (newId -> newCompany)
        newCompany

    override def getAll: Task[List[Company]] = ZIO.succeed:
        db.values.toList

    override def getById(id: Long): Task[Option[Company]] = ZIO.succeed:
        db.get(id)

    override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed:
        db.values.find(_.slug == slug)

object CompanyServiceDummy:
    val layer: ULayer[CompanyServiceDummy] = ZLayer.succeed(CompanyServiceDummy())
