package com.shadecode.life.core.model

/** A capability that develops through observable evidence and prerequisite skills. */
data class Skill(
    val id: String,
    val title: String,
    val domain: DevelopmentDomain,
    val description: String,
    val prerequisites: List<String> = emptyList(),
    val stageThresholds: List<Int> = listOf(1, 3, 5, 8)
)

data class SkillProgress(
    val skill: Skill,
    val evidenceCount: Int,
    val stage: SkillStage,
    val status: SkillStatus,
    val prerequisitesMet: Boolean
)

enum class SkillStage {
    FOUNDATION,
    DEVELOPING,
    FUNCTIONAL,
    RELIABLE,
    DEMONSTRATED
}

enum class SkillStatus {
    NOT_STARTED,
    IN_PROGRESS,
    DEMONSTRATED
}
