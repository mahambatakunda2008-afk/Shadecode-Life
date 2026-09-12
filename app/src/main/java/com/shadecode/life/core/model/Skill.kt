package com.shadecode.life.core.model

/** A capability that develops through observable evidence and prerequisite skills. */
data class Skill(
    val id: String,
    val title: String,
    val domain: DevelopmentDomain,
    val description: String,
    val prerequisites: List<String> = emptyList(),
    val stageThresholds: List<Int> = listOf(1, 3, 5, 8),
    val metricDirection: MetricDirection = MetricDirection.HIGHER_IS_BETTER
)

enum class MetricDirection {
    HIGHER_IS_BETTER,
    LOWER_IS_BETTER,
    NOT_COMPARABLE
}

data class SkillProgress(
    val skill: Skill,
    val evidenceCount: Int,
    val weightedEvidence: Double,
    val distinctEvidenceDays: Int,
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
