package com.shadecode.life.core.model

/** A concrete step the user can take to create useful evidence or improve a domain. */
data class DevelopmentAction(
    val id: String,
    val domain: DevelopmentDomain,
    val title: String,
    val reason: String,
    val estimatedMinutes: Int,
    val kind: ActionKind,
    val skillId: String? = null
)

enum class ActionKind {
    MEASURE,
    PRACTICE,
    REFLECT
}
