package com.shadecode.life.core.model

import java.time.Instant

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
