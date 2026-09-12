package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertEquals(com.shadecode.life.core.model.ActionKind.PRACTICE, action.kind)
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
}
