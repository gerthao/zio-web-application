package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint

trait BaseController:
    val routes: List[ServerEndpoint[Any, zio.Task]]
