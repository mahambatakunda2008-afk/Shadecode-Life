package com.shadecode.life.core.model

/** The major areas Shadecode Life can develop and measure. */
enum class DevelopmentDomain(
    val title: String,
    val description: String
) {
    BODY("Body", "Fitness, nutrition, sleep, and personal care"),
    MIND("Mind", "Learning, reasoning, knowledge, and mental performance"),
    CAPABILITY("Capability", "Technical and practical skills"),
    COMMUNICATION("Communication", "Speaking, writing, listening, and clarity"),
    SOCIAL("Social", "Relationships, cooperation, and social awareness"),
    DISCIPLINE("Discipline", "Consistency, focus, planning, and follow-through"),
    CHARACTER("Character", "Values, responsibility, integrity, and conduct"),
    FINANCE("Finance", "Money management and financial capability"),
    CAREER("Career", "Direction, professional growth, and opportunity"),
    ENVIRONMENT("Environment", "Organization, workspace, and surroundings"),
    CULTURE("Culture", "General knowledge, culture, and wider understanding")
}
