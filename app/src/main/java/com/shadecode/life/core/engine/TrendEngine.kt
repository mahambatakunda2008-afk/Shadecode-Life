package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Trend

/** Detects simple evidence trends without pretending that sparse data is certainty. */
object TrendEngine {
    fun trendFor(evidence: List<Evidence>): Trend {
        val ordered = evidence.sortedBy { it.recordedAt }
        if (ordered.size < 2) return Trend.UNKNOWN

        val numeric = ordered.mapNotNull { it.value }
        if (numeric.size >= 2) {
            val midpoint = numeric.size / 2
            val first = numeric.take(midpoint).average()
            val second = numeric.drop(midpoint).average()
            val tolerance = (kotlin.math.abs(first) * 0.05).coerceAtLeast(0.5)
            return when {
                second > first + tolerance -> Trend.IMPROVING
                second < first - tolerance -> Trend.DECLINING
                else -> Trend.STABLE
            }
        }

        return if (ordered.size >= 3) Trend.STABLE else Trend.UNKNOWN
    }
}
