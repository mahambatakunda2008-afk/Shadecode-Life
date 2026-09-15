package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentEvent
import com.shadecode.life.core.model.DevelopmentOutcome
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.EventType

class DevelopmentSession {
    private val evidence = mutableListOf<Evidence>()
    private val events = mutableListOf<DevelopmentEvent>()

    fun addEvidence(items: List<Evidence>) {
        evidence += items
        items.forEach { item ->
            events += DevelopmentEvent("baseline_${item.id}", item.title, item.domain, EventType.BASELINE, item.note ?: item.value?.let { "$it ${item.unit.orEmpty()}" } ?: "Evidence recorded.", item.recordedAt)
        }
    }

    fun recordAction(action: DevelopmentAction, outcome: DevelopmentOutcome) {
        val occurredAt = java.time.Instant.now()
        evidence += Evidence(
            id = "action_${action.id}_${evidence.size}", domain = action.domain, title = action.title,
            skillId = action.skillId, value = outcome.value, unit = outcome.unit,
            note = outcome.reflection.ifBlank { "Action completed." }, kind = evidenceKindFor(action),
            recordedAt = occurredAt, sourceActionId = action.id
        )
        events += DevelopmentEvent("action_${action.id}_${events.size}", action.title, action.domain, EventType.ACTION_COMPLETED, evidenceDetail(outcome), occurredAt)
        if (outcome.reflection.isNotBlank()) events += DevelopmentEvent("reflection_${action.id}_${events.size}", "Reflection", action.domain, EventType.REFLECTION, outcome.reflection, occurredAt)
    }

    private fun evidenceDetail(outcome: DevelopmentOutcome): String = buildString {
        append(outcome.reflection.ifBlank { "Action completed." })
        outcome.value?.let { append(" | Result: ").append(it); outcome.unit?.takeIf(String::isNotBlank)?.let { unit -> append(" ").append(unit) } }
    }

    private fun evidenceKindFor(action: DevelopmentAction): EvidenceKind = when {
        action.kind == ActionKind.MEASURE -> EvidenceKind.MEASUREMENT
        action.kind == ActionKind.REFLECT -> EvidenceKind.REFLECTION
        action.skillId == "build_artifact" -> EvidenceKind.ARTIFACT
        action.skillId == "clear_speaking" || action.skillId == "active_listening" -> EvidenceKind.COMMUNICATION
        action.domain == DevelopmentDomain.COMMUNICATION || action.domain == DevelopmentDomain.SOCIAL -> EvidenceKind.COMMUNICATION
        else -> EvidenceKind.COMPLETED_TASK
    }

    fun replaceState(savedEvidence: List<Evidence>, savedEvents: List<DevelopmentEvent>) { evidence.clear(); evidence += savedEvidence; events.clear(); events += savedEvents }
    fun evidence(): List<Evidence> = evidence.toList()
    fun events(): List<DevelopmentEvent> = events.sortedByDescending { it.occurredAt }
    fun states(): List<DevelopmentState> = DevelopmentEngine.buildStates(evidence)
    fun nextFocus(): DevelopmentState? = DevelopmentEngine.chooseNextFocus(states())
}
