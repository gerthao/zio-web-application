package com.rockthejvm

import zio.{Task, ZIO, ZLayer}

class ConnectionPool(nConnections: Int):
    def get: Task[Connection] = ZIO.succeed(Connection())

object ConnectionPool:
    def live(nConnections: Int): ZLayer[Any, Nothing, ConnectionPool] =
        ZLayer.succeed(ConnectionPool(nConnections))
