package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentDecisionEngine
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
        val action = DevelopmentAction("skill-body_capacity-2", DevelopmentDomain.BODY, "Repeat your physical baseline with strict form", "Test action", 15, ActionKind.PRACTICE, "body_capacity")
        session.recordAction(action, DevelopmentOutcome("Strict form felt controlled.", 12.0, "reps"))
        val evidence = session.evidence()
        assertEquals(1, evidence.size)
        assertEquals("body_capacity", evidence.single().skillId)
        assertEquals(12.0, evidence.single().value)
        assertEquals("reps", evidence.single().unit)
        assertEquals(action.id, evidence.single().sourceActionId)
        assertEquals(EvidenceKind.COMPLETED_TASK, evidence.single().kind)
        assertEquals("Strict form felt controlled.", evidence.single().note)
        val events = session.events()
        assertEquals(2, events.size)
        assertTrue(events.any { it.type == EventType.ACTION_COMPLETED && it.detail.contains("12.0 reps") })
        assertTrue(events.any { it.type == EventType.REFLECTION && it.detail == "Strict form felt controlled." })
    }

    @Test
    fun completedActionClosesTheLoopAndRecomputesRecommendation() {
        val session = DevelopmentSession()
        session.addEvidence(listOf(Evidence("baseline-body", DevelopmentDomain.BODY, "Initial physical baseline", "body_capacity", 10.0, "reps", kind = EvidenceKind.MEASUREMENT, recordedAt = Instant.parse("2026-09-10T10:00:00Z"))))
        val firstDecision = DevelopmentDecisionEngine.next(session.evidence())
        val firstAction = DevelopmentDecisionEngine.nextAction(session.evidence())
        assertNotNull(firstDecision); assertNotNull(firstAction)
        assertEquals(firstDecision.skill.id, firstAction.skillId)
        session.recordAction(firstAction, DevelopmentOutcome("The action was completed.", 12.0, "reps"))
        val updatedEvidence = session.evidence()
        assertTrue(updatedEvidence.any { it.sourceActionId == firstAction.id && it.skillId == firstAction.skillId && it.value == 12.0 })
        assertTrue(session.events().any { it.type == EventType.ACTION_COMPLETED })
        val nextDecision = DevelopmentDecisionEngine.next(updatedEvidence)
        val nextAction = DevelopmentDecisionEngine.nextAction(updatedEvidence)
        assertNotNull(nextDecision); assertNotNull(nextAction)
        assertEquals(nextDecision.skill.id, nextAction.skillId)
    }

    @Test
    fun replacingStateRestoresEvidenceAndEvents() {
        val original = DevelopmentSession()
        val action = DevelopmentAction("restore-action", DevelopmentDomain.MIND, "Explain a concept", "Test", 10, ActionKind.PRACTICE, "concept_explanation")
        original.recordAction(action, DevelopmentOutcome("Clear explanation.", null, null))
        val restored = DevelopmentSession()
        restored.replaceState(original.evidence(), original.events())
        assertEquals(original.evidence(), restored.evidence())
        assertEquals(original.events(), restored.events())
    }
}
