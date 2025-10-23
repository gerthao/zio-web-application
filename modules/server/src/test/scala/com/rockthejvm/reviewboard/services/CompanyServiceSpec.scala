package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import zio.*
import zio.test.*
import com.rockthejvm.reviewboard.syntax.*

object CompanyServiceSpec extends ZIOSpecDefault:
    val service: ZIO.ServiceWithZIOPartiallyApplied[CompanyService] =
        ZIO.serviceWithZIO[CompanyService]

    val stubRepositoryLayer: ULayer[CompanyRepository] = ZLayer.succeed:
        new CompanyRepository:
            val db = collection.mutable.Map.empty[Long, Company]

            override def create(company: Company): Task[Company] = ZIO.succeed:
                val newId      = db.keys.maxOption.getOrElse(0L) + 1
                val newCompany = company.copy(id = newId)
                db += (newId -> newCompany)
                newCompany

            override def update(id: Long, op: Company => Company): Task[Company] = ZIO.attempt:
                val company = db(id)
                db += (id -> op(company))
                company

            override def delete(id: Long): Task[Company] = ZIO.attempt:
                val company = db(id)
                db -= id
                company

            override def getById(id: Long): Task[Option[Company]] = ZIO.succeed:
                db.get(id)

            override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed:
                db.values.find(_.slug == slug)

            override def getAll: Task[List[Company]] = ZIO.succeed:
                db.values.toList

    val testCreate = test("create"):
        val companyZIO =
            service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))

        companyZIO.assert: company =>
            company.name == "Rock the JVM"
                && company.url == "rockthejvm.com"
                && company.slug == "rock-the-jvm"

    val testGetById = test("getById"):
        val program = for
            company    <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            companyOpt <- service(_.getById(company.id))
        yield (company, companyOpt)

        program.assert:
            case (company, Some(companyRes)) =>
                company.name == "Rock the JVM"
                && company.url == "rockthejvm.com"
                && company.slug == "rock-the-jvm"
                && company == companyRes
            case _ =>
                false

    override def spec: Spec[TestEnvironment & Scope, Any] = suite("CompanyServiceTest")(
        testCreate,
        testGetById
    ).provide(
        CompanyServiceLive.layer,
        stubRepositoryLayer
    )
