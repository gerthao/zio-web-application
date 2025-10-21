package com.rockthejvm.reviewboard.domain.data

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Review(
    id: Long, // Primary Key
    companyId: Long,
    userId: Long, // Foreign Key
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: java.time.Instant,
    updated: java.time.Instant
)

object Review:
    given code: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
