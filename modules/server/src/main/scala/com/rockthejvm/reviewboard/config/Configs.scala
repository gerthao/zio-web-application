package com.rockthejvm.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.config.magnolia
import zio.config.typesafe.TypesafeConfig
import zio.{Tag, ZIO, ZLayer}

object Configs:
    def makeConfigLayer[C](
        path: String
    )(using desc: magnolia.Descriptor[C], tag: Tag[C]): ZLayer[Any, Throwable, C] =
        TypesafeConfig.fromTypesafeConfig(
            ZIO.attempt(ConfigFactory.load().getConfig(path)),
            magnolia.descriptor[C]
        )
