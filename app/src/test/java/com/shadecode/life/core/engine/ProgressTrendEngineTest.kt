package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.MetricDirection
import com.shadecode.life.core.model.Skill
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressTrendEngineTest {
    private val skill = Skill(
        id = "test_capacity",
        title = "Test capacity",
        domain = DevelopmentDomain.BODY,
        description = "Test metric",
        metricDirection = MetricDirection.HIGHER_IS_BETTER
    )

    @Test
    fun higherValueIsImproving() {
        val trend = ProgressTrendEngine.forSkill(
            skill,
            listOf(measure(10.0, "reps", 1), measure(12.0, "reps", 2))
        )
        assertEquals(ProgressTrendEngine.TrendDirection.IMPROVING, trend.direction)
        assertEquals(2.0, trend.change)
    }

    @Test
    fun lowerValueIsDecliningWhenHigherIsBetter() {
        val trend = ProgressTrendEngine.forSkill(
            skill,
            listOf(measure(12.0, "reps", 1), measure(10.0, "reps", 2))
        )
        assertEquals(ProgressTrendEngine.TrendDirection.DECLINING, trend.direction)
    }

    @Test
    fun incompatibleUnitsAreNotCompared() {
        val trend = ProgressTrendEngine.forSkill(
            skill,
            listOf(measure(10.0, "reps", 1), measure(30.0, "seconds", 2))
        )
        assertEquals(ProgressTrendEngine.TrendDirection.INSUFFICIENT_DATA, trend.direction)
        assertEquals(1, trend.measuredCount)
    }

    private fun measure(value: Double, unit: String, day: Long) = Evidence(
        id = "$unit-$day",
        domain = DevelopmentDomain.BODY,
        title = "Baseline",
        skillId = skill.id,
        value = value,
        unit = unit,
        kind = EvidenceKind.MEASUREMENT,
        recordedAt = java.time.Instant.parse("2026-09-${"%02d".format(day)}T10:00:00Z")
    )
}
