package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DevelopmentDecisionEngineTest {
    @Test
    fun firstDecisionRequestsMeasurement() {
        val decision = DevelopmentDecisionEngine.next(emptyList())
        assertNotNull(decision)
        assertEquals(ProgressTrendEngine.TrendDirection.INSUFFICIENT_DATA, decision.trend)
        assertEquals(com.shadecode.life.core.model.ActionKind.MEASURE, DevelopmentDecisionEngine.actionFor(decision).kind)
    }

    @Test
    fun improvingEvidenceChangesActionReason() {
        val skillId = "body_capacity"
        val evidence = listOf(
            evidence(skillId, 10.0, "reps", 1),
            evidence(skillId, 14.0, "reps", 2)
        )
        val decision = DevelopmentDecisionEngine.next(evidence)
        assertNotNull(decision)
        assertEquals(ProgressTrendEngine.TrendDirection.IMPROVING, decision.trend)
        assertTrue(DevelopmentDecisionEngine.actionFor(decision).reason.contains("improving", ignoreCase = true))
    }

    @Test
    fun decliningEvidenceChangesActionReason() {
        val skillId = "body_capacity"
        val evidence = listOf(
            evidence(skillId, 14.0, "reps", 1),
            evidence(skillId, 10.0, "reps", 2)
        )
        val decision = DevelopmentDecisionEngine.next(evidence)
        assertNotNull(decision)
        assertEquals(ProgressTrendEngine.TrendDirection.DECLINING, decision.trend)
        assertTrue(DevelopmentDecisionEngine.actionFor(decision).reason.contains("rebuild", ignoreCase = true))
    }

    private fun evidence(skillId: String, value: Double, unit: String, day: Long) = Evidence(
        id = "$skillId-$day",
        domain = DevelopmentDomain.BODY,
        title = "Physical baseline",
        skillId = skillId,
        value = value,
        unit = unit,
        kind = EvidenceKind.MEASUREMENT,
        recordedAt = java.time.Instant.parse("2026-09-${"%02d".format(day)}T10:00:00Z")
    )
}
