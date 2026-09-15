package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import java.time.Instant

class DevelopmentDecisionEngineTest {
    private val skill = Skill(
        id = "body_capacity",
        title = "Physical capacity",
        domain = DevelopmentDomain.BODY,
        description = "A measurable physical capability"
    )

    @Test
    fun improvingDecisionChangesActionReason() {
        val decision = decision(ProgressTrendEngine.TrendDirection.IMPROVING)
        val action = DevelopmentDecisionEngine.actionFor(decision)
        assertEquals(ActionKind.PRACTICE, action.kind)
        assertTrue(action.reason.contains("improving", ignoreCase = true))
    }

    @Test
    fun decliningDecisionPrioritizesRebuilding() {
        val decision = decision(ProgressTrendEngine.TrendDirection.DECLINING)
        val action = DevelopmentDecisionEngine.actionFor(decision)
        assertTrue(action.reason.contains("rebuild", ignoreCase = true))
    }

    @Test
    fun stableDecisionChangesContext() {
        val decision = decision(ProgressTrendEngine.TrendDirection.STABLE)
        val action = DevelopmentDecisionEngine.actionFor(decision)
        assertTrue(action.reason.contains("context", ignoreCase = true))
    }

    @Test
    fun repeatedEvidenceDoesNotRepeatRecentActionTitleWhenVariantExists() {
        val repeatedTitle = "Repeat your physical baseline with strict form"
        val decision = decision(ProgressTrendEngine.TrendDirection.INSUFFICIENT_DATA).copy(
            recentEvidenceTitles = listOf(repeatedTitle)
        )
        val action = DevelopmentDecisionEngine.actionFor(decision)
        assertNotEquals(repeatedTitle, action.title)
    }

    @Test
    fun realDeclineProducesDecliningDecision() {
        val evidence = listOf(
            measurement(12.0, "reps", "2026-09-10T10:00:00Z"),
            measurement(8.0, "reps", "2026-09-12T10:00:00Z")
        )
        val decision = DevelopmentDecisionEngine.Decision(
            skill = skill,
            progress = SkillProgress(
                skill = skill,
                evidenceCount = 2,
                weightedEvidence = 2.0,
                distinctEvidenceDays = 2,
                stage = SkillStage.DEVELOPING,
                status = SkillStatus.IN_PROGRESS,
                prerequisitesMet = true
            ),
            reason = "Test",
            priority = 1.0,
            recentEvidenceTitles = evidence.map { it.title },
            trend = ProgressTrendEngine.forSkill(skill, evidence).direction
        )
        assertEquals(ProgressTrendEngine.TrendDirection.DECLINING, decision.trend)
        assertTrue(DevelopmentDecisionEngine.actionFor(decision).reason.contains("rebuild", ignoreCase = true))
    }

    private fun decision(trend: ProgressTrendEngine.TrendDirection) =
        DevelopmentDecisionEngine.Decision(
            skill = skill,
            progress = SkillProgress(
                skill = skill,
                evidenceCount = 2,
                weightedEvidence = 2.0,
                distinctEvidenceDays = 2,
                stage = SkillStage.DEVELOPING,
                status = SkillStatus.IN_PROGRESS,
                prerequisitesMet = true
            ),
            reason = "Test decision",
            priority = 1.0,
            recentEvidenceTitles = emptyList(),
            trend = trend
        )

    private fun measurement(value: Double, unit: String, recordedAt: String) = Evidence(
        id = "$unit-$value",
        domain = DevelopmentDomain.BODY,
        title = "Physical baseline",
        skillId = skill.id,
        value = value,
        unit = unit,
        kind = EvidenceKind.MEASUREMENT,
        recordedAt = Instant.parse(recordedAt)
    )
}
