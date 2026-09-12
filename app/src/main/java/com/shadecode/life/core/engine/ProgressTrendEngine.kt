package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.MetricDirection
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import java.time.Instant

/**
 * Deterministic trend analysis for the personal model.
 * Numeric evidence is compared in chronological order; non-numeric evidence is tracked
 * as repeat activity without inventing a performance score.
 */
object ProgressTrendEngine {
    data class Trend(
        val skill: Skill,
        val direction: TrendDirection,
        val latestValue: Double?,
        val previousValue: Double?,
        val change: Double?,
        val evidenceCount: Int,
        val measuredCount: Int,
        val firstRecordedAt: Instant?,
        val latestRecordedAt: Instant?
    )

    enum class TrendDirection {
        IMPROVING,
        DECLINING,
        STABLE,
        INSUFFICIENT_DATA
    }

    fun all(evidence: List<Evidence>): List<Trend> =
        SkillCatalog.all.map { trendFor(it, evidence) }

    fun forSkill(skill: Skill, evidence: List<Evidence>): Trend = trendFor(skill, evidence)

    private fun trendFor(skill: Skill, evidence: List<Evidence>): Trend {
        val relevant = evidence
            .filter { it.skillId == skill.id || (it.skillId == null && it.domain == skill.domain) }
            .sortedBy { it.recordedAt }
        val numeric = relevant.filter { it.value != null }
        val latest = numeric.lastOrNull()
        val previous = numeric.dropLast(1).lastOrNull()
        val change = if (latest != null && previous != null) latest.value!! - previous.value!! else null

        val direction = when {
            skill.metricDirection == MetricDirection.NOT_COMPARABLE -> TrendDirection.INSUFFICIENT_DATA
            numeric.size < 2 -> TrendDirection.INSUFFICIENT_DATA
            change == null -> TrendDirection.INSUFFICIENT_DATA
            kotlin.math.abs(change) <= tolerance(previous!!.value!!) -> TrendDirection.STABLE
            skill.metricDirection == MetricDirection.HIGHER_IS_BETTER && change > 0 -> TrendDirection.IMPROVING
            skill.metricDirection == MetricDirection.LOWER_IS_BETTER && change < 0 -> TrendDirection.IMPROVING
            else -> TrendDirection.DECLINING
        }

        return Trend(
            skill = skill,
            direction = direction,
            latestValue = latest?.value,
            previousValue = previous?.value,
            change = change,
            evidenceCount = relevant.size,
            measuredCount = numeric.size,
            firstRecordedAt = relevant.firstOrNull()?.recordedAt,
            latestRecordedAt = relevant.lastOrNull()?.recordedAt
        )
    }

    private fun tolerance(value: Double): Double =
        (kotlin.math.abs(value) * 0.05).coerceAtLeast(0.5)
}
