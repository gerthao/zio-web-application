package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.syntax.*
import org.postgresql.ds.PGSimpleDataSource
import zio.*
import zio.test.*
import org.testcontainers.containers.PostgreSQLContainer

import java.sql.SQLException
import javax.sql.DataSource
import scala.util.Random

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/companies.sql"):
    private val rtjvm = Company(
        id   = 1L,
        slug = "rock-the-jvm",
        name = "Rock the JVM",
        url  = "rockthejvm.com"
    )

    private def genCompany(): Company = Company(
        id   = -1L,
        slug = genString(),
        name = genString(),
        url  = genString(),
    )

    private def genString(): String =
        Random.alphanumeric.take(8).mkString

    override def spec: Spec[TestEnvironment & Scope, Any] =
        suite("CompanyRepositorySpec")(
            test("create a company"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    company <- repo.create(rtjvm)
                yield company

                program.assert:
                    case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
                    case _ => false,
            
            test("creating a duplicate should error"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    _ <- repo.create(rtjvm)
                    err <- repo.create(rtjvm).flip
                yield err
                
                program.assert(_.isInstanceOf[SQLException]),
            
            test("get by id and slug"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    company <- repo.create(rtjvm)
                    fetchedById <- repo.getById(company.id)
                    fetchedBySlug <- repo.getBySlug(company.slug)
                yield (company, fetchedById, fetchedBySlug)

                program.assert:
                    case (company, fetchedById, fetchedBySlug) =>
                        fetchedById.contains(company)
                            && fetchedBySlug.contains(company),
            
            test("updated record"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    company <- repo.create(rtjvm)
                    updated <- repo.update(company.id, _.copy(url = "blog.rockthejvm.xyz"))
                    fetchedById <- repo.getById(updated.id)
                yield (updated, fetchedById)

                program.assert:
                    case (updated, fetchedById) =>
                        fetchedById.contains(updated),
            
            test("delete record"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    company <- repo.create(rtjvm)
                    _ <- repo.delete(company.id)
                    fetchedById <- repo.getById(company.id)
                yield fetchedById
                
                program.assert(_.isEmpty),
            
            test("get all records"):
                val program = for
                    repo <- ZIO.service[CompanyRepository]
                    companies <- ZIO.collectAll((1 to 10).map(_ => repo.create(genCompany())))
                    companiesFetched <- repo.getAll
                yield (companies, companiesFetched)

                program.assert:
                    case (companies, companiesFetched) =>
                        companies.toSet == companiesFetched.toSet

        ).provide(
            CompanyRepositoryLive.layer,
            dataSourceLayer,
            Repository.quillLayer,
            Scope.default
        )