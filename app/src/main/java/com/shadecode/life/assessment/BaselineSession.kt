package com.shadecode.life.assessment

import com.shadecode.life.core.model.BaselineItem
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import java.time.Instant

class BaselineSession(
    val items: List<BaselineItem>
) {
    private val answers = mutableMapOf<String, String>()
    private val recordedAt = mutableMapOf<String, Instant>()

    fun record(item: BaselineItem, answer: String) {
        val normalized = answer.trim()
        answers[item.id] = normalized
        if (normalized.isBlank()) {
            recordedAt.remove(item.id)
        } else {
            recordedAt.putIfAbsent(item.id, Instant.now())
        }
    }

    fun answerFor(itemId: String): String = answers[itemId].orEmpty()

    fun evidenceFor(itemId: String): Evidence? = evidence().firstOrNull { it.id == "baseline_$itemId" }

    fun evidence(): List<Evidence> = items.mapNotNull { item ->
        val answer = answers[item.id].orEmpty()
        if (answer.isBlank()) return@mapNotNull null

        Evidence(
            id = "baseline_${item.id}",
            domain = item.domain,
            title = item.title,
            skillId = item.skillId,
            value = item.unit?.let { answer.toDoubleOrNull() },
            unit = item.unit,
            note = if (item.unit == null) answer else null,
            kind = when (item.domain) {
                DevelopmentDomain.COMMUNICATION -> EvidenceKind.COMMUNICATION
                else -> if (item.unit != null) EvidenceKind.MEASUREMENT else EvidenceKind.OBSERVATION
            },
            recordedAt = recordedAt[item.id] ?: Instant.now()
        )
    }

    fun isComplete(): Boolean = items.all { answerFor(it.id).isNotBlank() }
}
