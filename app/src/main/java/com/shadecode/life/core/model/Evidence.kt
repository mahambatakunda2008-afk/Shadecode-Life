package com.shadecode.life.core.model

import java.time.Instant

/** Observable input used to understand development without relying on arbitrary self-ratings. */
data class Evidence(
    val id: String,
    val domain: DevelopmentDomain,
    val title: String,
    val value: Double? = null,
    val unit: String? = null,
    val note: String? = null,
    val recordedAt: Instant = Instant.now()
)
