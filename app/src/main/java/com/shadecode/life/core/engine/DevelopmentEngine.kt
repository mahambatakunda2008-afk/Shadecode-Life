package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Trend

/**
 * Small deterministic engine for v0.1.
 * It deliberately stays explainable before any AI layer is introduced.
 */
object DevelopmentEngine {
    fun buildStates(evidence: List<Evidence>): List<DevelopmentState> =
        DevelopmentDomain.entries.map { domain ->
            val domainEvidence = evidence.filter { it.domain == domain }
            DevelopmentState(
                domain = domain,
                evidenceCount = domainEvidence.size,
                confidence = confidence(domainEvidence.size),
                trend = Trend.UNKNOWN
            )
        }

    fun chooseNextFocus(states: List<DevelopmentState>): DevelopmentState? =
        states
            .filter { it.evidenceCount == 0 }
            .minByOrNull { foundationOrder(it.domain) }
            ?: states.minByOrNull { it.confidence }

    fun recommendNextAction(states: List<DevelopmentState>): DevelopmentAction? {
        val focus = chooseNextFocus(states) ?: return null
        return if (focus.evidenceCount == 0) {
            DevelopmentAction(
                id = "measure_${focus.domain.name.lowercase()}",
                domain = focus.domain,
                title = "Measure your ${focus.domain.title.lowercase()}",
                reason = "We do not have enough evidence yet. A small measurement gives us a better starting point than guessing.",
                estimatedMinutes = 5,
                kind = ActionKind.MEASURE
            )
        } else {
            DevelopmentAction(
                id = "practice_${focus.domain.name.lowercase()}",
                domain = focus.domain,
                title = "Take one deliberate step in ${focus.domain.title.lowercase()}",
                reason = "This area has the least evidence confidence, so improving it is the current highest-leverage move.",
                estimatedMinutes = 15,
                kind = ActionKind.PRACTICE
            )
        }
    }

    private fun confidence(evidenceCount: Int): Double =
        when (evidenceCount) {
            0 -> 0.0
            1 -> 0.35
            2 -> 0.55
            3 -> 0.75
            else -> 0.9
        }

    private fun foundationOrder(domain: DevelopmentDomain): Int =
        when (domain) {
            DevelopmentDomain.BODY -> 0
            DevelopmentDomain.MIND -> 1
            DevelopmentDomain.DISCIPLINE -> 2
            DevelopmentDomain.ENVIRONMENT -> 3
            DevelopmentDomain.COMMUNICATION -> 4
            DevelopmentDomain.CAPABILITY -> 5
            DevelopmentDomain.FINANCE -> 6
            DevelopmentDomain.SOCIAL -> 7
            DevelopmentDomain.CHARACTER -> 8
            DevelopmentDomain.CAREER -> 9
            DevelopmentDomain.CULTURE -> 10
        }
}
