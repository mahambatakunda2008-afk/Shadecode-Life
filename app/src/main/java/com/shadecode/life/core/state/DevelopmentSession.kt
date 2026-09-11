package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentEvent
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EventType

class DevelopmentSession {
    private val evidence = mutableListOf<Evidence>()
    private val events = mutableListOf<DevelopmentEvent>()

    fun addEvidence(items: List<Evidence>) {
        evidence += items
        items.forEach { item ->
            events += DevelopmentEvent(
                id = "baseline_${item.id}",
                title = item.title,
                domain = item.domain,
                type = EventType.BASELINE,
                detail = item.note ?: item.value?.let { "$it ${item.unit.orEmpty()}" } ?: "Evidence recorded.",
                occurredAt = item.recordedAt
            )
        }
    }

    fun recordAction(action: DevelopmentAction, reflection: String) {
        val detail = reflection.ifBlank { "Action completed." }
        val occurredAt = java.time.Instant.now()
        evidence += Evidence(
            id = "action_${action.id}_${evidence.size}",
            domain = action.domain,
            title = action.title,
            skillId = action.skillId,
            note = detail,
            recordedAt = occurredAt
        )
        events += DevelopmentEvent(
            id = "action_${action.id}_${events.size}",
            title = action.title,
            domain = action.domain,
            type = EventType.ACTION_COMPLETED,
            detail = detail,
            occurredAt = occurredAt
        )
        if (reflection.isNotBlank()) {
            events += DevelopmentEvent(
                id = "reflection_${action.id}_${events.size}",
                title = "Reflection",
                domain = action.domain,
                type = EventType.REFLECTION,
                detail = reflection,
                occurredAt = occurredAt
            )
        }
    }

    fun replaceState(savedEvidence: List<Evidence>, savedEvents: List<DevelopmentEvent>) {
        evidence.clear()
        evidence += savedEvidence
        events.clear()
        events += savedEvents
    }

    fun evidence(): List<Evidence> = evidence.toList()
    fun events(): List<DevelopmentEvent> = events.sortedByDescending { it.occurredAt }
    fun states(): List<DevelopmentState> = DevelopmentEngine.buildStates(evidence)
    fun nextFocus(): DevelopmentState? = DevelopmentEngine.chooseNextFocus(states())
}
