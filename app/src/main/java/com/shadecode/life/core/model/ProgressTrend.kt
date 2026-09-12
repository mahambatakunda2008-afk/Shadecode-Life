package com.shadecode.life.core.model

/** Describes change in a measurable capability without reducing development to a single score. */
data class ProgressTrend(
    val skillId: String,
    val latestValue: Double?,
    val previousValue: Double?,
    val change: Double?,
    val direction: TrendDirection,
    val sampleCount: Int,
    val distinctDays: Int,
    val unit: String?
)

enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE,
    INSUFFICIENT_DATA
}
