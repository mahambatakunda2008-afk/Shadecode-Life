package com.shadecode.life.assessment

import com.shadecode.life.core.model.BaselineItem
import com.shadecode.life.core.model.Evidence

/** Interprets a completed baseline task without pretending the result is a clinical or absolute score. */
data class AssessmentResult(
    val item: BaselineItem,
    val evidence: Evidence,
    val interpretation: String,
    val nextStep: String
)

object AssessmentInterpreter {
    fun interpret(item: BaselineItem, evidence: Evidence): AssessmentResult {
        val interpretation = when {
            evidence.value != null -> "Recorded result: ${evidence.value} ${evidence.unit.orEmpty()}. This becomes a reference point for future comparisons."
            evidence.note?.isNotBlank() == true -> "Recorded observation: ${evidence.note}. Future observations can show whether this capability is becoming more consistent."
            else -> "An observation was recorded. More evidence is needed before making a strong conclusion."
        }

        val nextStep = when {
            evidence.value != null -> "Repeat this measurement on another day under similar conditions."
            else -> "Repeat the task in a different real situation and record what changed."
        }

        return AssessmentResult(
            item = item,
            evidence = evidence,
            interpretation = interpretation,
            nextStep = nextStep
        )
    }
}
