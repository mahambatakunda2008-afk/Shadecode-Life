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
                trend = TrendEngine.trendFor(domainEvidence)
            )
        }

    fun chooseNextFocus(states: List<DevelopmentState>): DevelopmentState? =
        states
            .filter { it.evidenceCount == 0 }
            .minByOrNull { foundationOrder(it.domain) }
            ?: states.minWithOrNull(
                compareBy<DevelopmentState>({ trendPriority(it.trend) }, { it.confidence }, { foundationOrder(it.domain) })
            )

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
            val title = when (focus.trend) {
                Trend.DECLINING -> "Stabilize your ${focus.domain.title.lowercase()}"
                Trend.IMPROVING -> "Strengthen your ${focus.domain.title.lowercase()}"
                else -> "Take one deliberate step in ${focus.domain.title.lowercase()}"
            }
            DevelopmentAction(
                id = "practice_${focus.domain.name.lowercase()}",
                domain = focus.domain,
                title = title,
                reason = reasonFor(focus),
                estimatedMinutes = 15,
                kind = ActionKind.PRACTICE
            )
        }
    }

    private fun reasonFor(state: DevelopmentState): String = when (state.trend) {
        Trend.DECLINING -> "Recent evidence suggests this area is slipping. Stabilizing it takes priority over adding another goal."
        Trend.IMPROVING -> "Recent evidence is improving. A deliberate next step can turn that momentum into a repeatable capability."
        Trend.STABLE -> "Recent evidence is stable. A focused practice step can test whether the capability can move beyond its current level."
        Trend.UNKNOWN -> "This area has the least evidence confidence, so another useful observation is the current highest-leverage move."
    }

    private fun trendPriority(trend: Trend): Int = when (trend) {
        Trend.DECLINING -> 0
        Trend.UNKNOWN -> 1
        Trend.STABLE -> 2
        Trend.IMPROVING -> 3
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
