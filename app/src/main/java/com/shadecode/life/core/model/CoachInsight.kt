package com.shadecode.life.core.model

data class CoachInsight(
    val title: String,
    val message: String,
    val reason: String,
    val action: DevelopmentAction?,
    val skill: SkillProgress?,
    val priority: Priority,
    val evidenceSummary: String
)

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}
