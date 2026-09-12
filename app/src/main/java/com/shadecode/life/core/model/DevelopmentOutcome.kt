package com.shadecode.life.core.model

/** Structured result captured when a development action is completed. */
data class DevelopmentOutcome(
    val reflection: String,
    val value: Double? = null,
    val unit: String? = null
)
