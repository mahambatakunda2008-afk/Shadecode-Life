package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DailyPlan
import com.shadecode.life.core.model.DevelopmentState

/** Converts the current personal model into one focused, explainable daily plan. */
object DailyPlanner {
    fun create(states: List<DevelopmentState>): DailyPlan? {
        val focus = DevelopmentEngine.chooseNextFocus(states) ?: return null
        val action = DevelopmentEngine.recommendNextAction(states) ?: return null

        val explanation = if (focus.evidenceCount == 0) {
            "This is a new area in your model. We measure it before deciding what improvement means."
        } else {
            "This area currently has the lowest confidence, so today's action is chosen to create useful evidence."
        }

        return DailyPlan(
            focus = focus,
            action = action,
            explanation = explanation
        )
    }
}
