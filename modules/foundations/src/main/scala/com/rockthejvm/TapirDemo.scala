package com.rockthejvm

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server

object TapirDemo extends ZIOAppDefault:
    val simplestEndpoint = endpoint
        .tag("simple")
        .name("simple")
        .description("simplest endpoint possible")
        // ^^ for documentation
        .get                    // http method
        .in("simple")           // path
        .out(plainBody[String]) // output
        .serverLogicSuccess[zio.Task](_ => ZIO.succeed("All good!"))

    val db: scala.collection.mutable.Map[Long, Job] =
        scala.collection.mutable.Map(
          1L -> Job(id = 1L, title = "Instructor", url = "rockthejvm.com", company = "Rock the JVM")
        )

    val createEndpoint: ServerEndpoint[Any, zio.Task] = endpoint
        .tag("jobs")
        .name("create")
        .description("Create a job")
        .in("jobs")
        .post
        .in(jsonBody[CreateJobRequest])
        .out(jsonBody[Job])
        .serverLogicSuccess[zio.Task]: request =>
            ZIO.succeed:
                val newId = db.keys.max + 1
                val newJob =
                    Job(newId, request.title, request.url, request.company)
                db += (newId -> newJob)
                newJob

    val getByIdEndpoint: ServerEndpoint[Any, zio.Task] = endpoint
        .tag("jobs")
        .name("getById")
        .description("Get job by id")
        .in("jobs" / path[Long]("id"))
        .get
        .out(jsonBody[Option[Job]])
        .serverLogicSuccess[zio.Task]: id =>
            ZIO.succeed(db.get(id))

    val getAllEndpoint: ServerEndpoint[Any, zio.Task] = endpoint
        .tag("jobs")
        .name("getAll")
        .description("Get all jobs")
        .in("jobs")
        .get
        .out(jsonBody[List[Job]])
        .serverLogicSuccess[zio.Task](_ => ZIO.succeed(db.values.toList))

    val serverProgram = Server.serve(
        ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(
          List(createEndpoint, getByIdEndpoint, getAllEndpoint)
        )
    )

    override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = serverProgram.provide(Server.default)
