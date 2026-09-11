package com.shadecode.life.core.model

/** A small, explainable plan for one development cycle. */
data class DailyPlan(
    val focus: DevelopmentState,
    val action: DevelopmentAction,
    val explanation: String
)
