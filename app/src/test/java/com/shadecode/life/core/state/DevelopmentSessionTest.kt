package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentDecisionEngine
import com.shadecode.life.core.engine.ProgressTrendEngine
import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentOutcome
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.time.Instant

class DevelopmentSessionTest {
    @Test
    fun recordActionTurnsStructuredOutcomeIntoEvidenceAndEvents() {
        val session = DevelopmentSession()
        val action = DevelopmentAction(
            id = "skill-body_capacity-2",
            domain = DevelopmentDomain.BODY,
            title = "Repeat your physical baseline with strict form",
            reason = "Test action",
            estimatedMinutes = 15,
            kind = ActionKind.PRACTICE,
            skillId = "body_capacity"
        )

        session.recordAction(
            action,
            DevelopmentOutcome(
                reflection = "Strict form felt controlled.",
                value = 12.0,
                unit = "reps"
            )
        )

        val evidence = session.evidence()
        assertEquals(1, evidence.size)
        assertEquals("body_capacity", evidence.single().skillId)
        assertEquals(12.0, evidence.single().value)
        assertEquals("reps", evidence.single().unit)
        assertEquals(EvidenceKind.COMPLETED_TASK, evidence.single().kind)
        assertEquals("Strict form felt controlled.", evidence.single().note)

        val events = session.events()
        assertEquals(2, events.size)
        assertTrue(events.any { it.type == EventType.ACTION_COMPLETED && it.detail.contains("12.0 reps") })
        assertTrue(events.any { it.type == EventType.REFLECTION && it.detail == "Strict form felt controlled." })
    }

    @Test
    fun completedActionClosesTheLoopAndChangesNextRecommendation() {
        val session = DevelopmentSession()
        session.addEvidence(
            listOf(
                Evidence(
                    id = "baseline-body",
                    domain = DevelopmentDomain.BODY,
                    title = "Initial physical baseline",
                    skillId = "body_capacity",
                    value = 10.0,
                    unit = "reps",
                    kind = EvidenceKind.MEASUREMENT,
                    recordedAt = Instant.parse("2026-09-10T10:00:00Z")
                )
            )
        )

        val firstDecision = DevelopmentDecisionEngine.next(session.evidence())
        val firstAction = DevelopmentDecisionEngine.nextAction(session.evidence())
        assertNotNull(firstDecision)
        assertNotNull(firstAction)
        assertEquals("body_capacity", firstDecision.skill.id)
        assertEquals(ActionKind.PRACTICE, firstAction.kind)

        session.recordAction(
            firstAction,
            DevelopmentOutcome(
                reflection = "The second attempt was stronger.",
                value = 12.0,
                unit = "reps"
            )
        )

        val updatedEvidence = session.evidence()
        assertEquals(2, updatedEvidence.count { it.skillId == "body_capacity" })

        val trend = ProgressTrendEngine.forSkill(firstDecision.skill, updatedEvidence)
        assertEquals(ProgressTrendEngine.TrendDirection.IMPROVING, trend.direction)
        assertEquals(2.0, trend.change)

        val nextDecision = DevelopmentDecisionEngine.next(updatedEvidence)
        val nextAction = DevelopmentDecisionEngine.nextAction(updatedEvidence)
        assertNotNull(nextDecision)
        assertNotNull(nextAction)
        assertEquals(ProgressTrendEngine.TrendDirection.IMPROVING, nextDecision.trend)
        assertTrue(nextAction.reason.contains("improving", ignoreCase = true))
        assertTrue(nextAction.title != firstAction.title)
        assertTrue(nextAction.id != firstAction.id)
    }
}
