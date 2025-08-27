package com.rockthejvm.reviewboard

import scala.annotation.targetName

object PipeOps:
    extension [T](x: T)
        @targetName("pipe")
        inline def |>[U] (f: T => U): U = f(x)