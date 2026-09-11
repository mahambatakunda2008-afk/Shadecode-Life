package com.shadecode.life.core.model

import java.time.Instant

/** A chronological record of something that changed the personal model. */
data class DevelopmentEvent(
    val id: String,
    val title: String,
    val domain: DevelopmentDomain,
    val type: EventType,
    val detail: String,
    val occurredAt: Instant = Instant.now()
)

enum class EventType {
    BASELINE,
    ACTION_COMPLETED,
    REFLECTION
}
