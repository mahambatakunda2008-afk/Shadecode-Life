package com.shadecode.life.core.state

import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence

class DevelopmentSession {
    private val evidence = mutableListOf<Evidence>()

    fun addEvidence(items: List<Evidence>) {
        evidence += items
    }

    fun evidence(): List<Evidence> = evidence.toList()

    fun states(): List<DevelopmentState> = DevelopmentEngine.buildStates(evidence)

    fun nextFocus(): DevelopmentState? = DevelopmentEngine.chooseNextFocus(states())
}
