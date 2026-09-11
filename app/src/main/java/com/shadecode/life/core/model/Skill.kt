package com.shadecode.life.core.model

/** A capability that can be developed through observable evidence. */
data class Skill(
    val id: String,
    val title: String,
    val domain: DevelopmentDomain,
    val description: String,
    val prerequisites: List<String> = emptyList()
)

data class SkillProgress(
    val skill: Skill,
    val evidenceCount: Int,
    val status: SkillStatus
)

enum class SkillStatus {
    NOT_STARTED,
    IN_PROGRESS,
    DEMONSTRATED
}
