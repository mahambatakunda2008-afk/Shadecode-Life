package com.shadecode.life.core.model

/** Current state of one development domain, derived from evidence rather than a self-rating. */
data class DevelopmentState(
    val domain: DevelopmentDomain,
    val evidenceCount: Int = 0,
    val confidence: Double = 0.0,
    val trend: Trend = Trend.UNKNOWN,
    val nextActionId: String? = null
)

enum class Trend {
    IMPROVING,
    STABLE,
    DECLINING,
    UNKNOWN
}
