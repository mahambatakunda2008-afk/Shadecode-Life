package com.shadecode.life.core.model

/** A user-directed outcome connected to a capability, not a generic checkbox. */
data class DevelopmentGoal(
    val id: String,
    val title: String,
    val domain: DevelopmentDomain,
    val description: String,
    val targetSkillId: String,
    val milestones: List<GoalMilestone> = emptyList(),
    val status: GoalStatus = GoalStatus.ACTIVE
)

data class GoalMilestone(
    val id: String,
    val title: String,
    val requiredEvidence: Int,
    val completed: Boolean = false
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED
}
