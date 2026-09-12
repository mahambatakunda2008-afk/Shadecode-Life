package com.shadecode.life.core.model

import java.time.Instant

/** Observable input used to understand development without relying on arbitrary self-ratings. */
data class Evidence(
    val id: String,
    val domain: DevelopmentDomain,
    val title: String,
    val skillId: String? = null,
    val value: Double? = null,
    val unit: String? = null,
    val note: String? = null,
    val kind: EvidenceKind = EvidenceKind.OBSERVATION,
    val recordedAt: Instant = Instant.now(),
    /** Links outcome evidence back to the action that produced it. Null for baseline/manual evidence. */
    val sourceActionId: String? = null
)

enum class EvidenceKind {
    MEASUREMENT,
    COMPLETED_TASK,
    ARTIFACT,
    COMMUNICATION,
    REFLECTION,
    OBSERVATION
}
