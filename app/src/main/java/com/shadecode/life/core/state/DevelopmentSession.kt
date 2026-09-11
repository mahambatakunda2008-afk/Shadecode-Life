package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence

class DevelopmentSession {
    private val evidence = mutableListOf<Evidence>()

    fun addEvidence(items: List<Evidence>) {
        evidence += items
    }

    fun recordAction(action: DevelopmentAction, reflection: String) {
        evidence += Evidence(
            id = "action_${action.id}_${evidence.size}",
            domain = action.domain,
            title = action.title,
            note = reflection.ifBlank { "Action completed." }
        )
    }

    fun evidence(): List<Evidence> = evidence.toList()

    fun states(): List<DevelopmentState> = DevelopmentEngine.buildStates(evidence)

    fun nextFocus(): DevelopmentState? = DevelopmentEngine.chooseNextFocus(states())
}
