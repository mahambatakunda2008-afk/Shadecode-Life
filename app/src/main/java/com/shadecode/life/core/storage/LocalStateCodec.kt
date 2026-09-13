package com.shadecode.life.core.storage

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentEvent
import com.shadecode.life.core.model.EventType
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import java.time.Instant
import java.util.Base64

internal object LocalStateCodec {
    fun encodeRecords(items: List<Evidence>): String = items.joinToString("\n") { item ->
        listOf(
            item.id,
            item.domain.name,
            item.title,
            item.skillId.orEmpty(),
            item.value?.toString().orEmpty(),
            item.unit.orEmpty(),
            item.note.orEmpty(),
            item.kind.name,
            item.recordedAt.toEpochMilli().toString()
        ).joinToString("|") { encode(it) }
    }

    fun decodeRecords(raw: String): List<Evidence> = raw.lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            runCatching {
                val parts = line.split('|').map(::decode)
                require(parts.size == 9)
                Evidence(
                    id = parts[0],
                    domain = DevelopmentDomain.valueOf(parts[1]),
                    title = parts[2],
                    skillId = parts[3].takeIf(String::isNotBlank),
                    value = parts[4].takeIf(String::isNotBlank)?.toDouble(),
                    unit = parts[5].takeIf(String::isNotBlank),
                    note = parts[6].takeIf(String::isNotBlank),
                    kind = EvidenceKind.valueOf(parts[7]),
                    recordedAt = Instant.ofEpochMilli(parts[8].toLong())
                )
            }.getOrNull()
        }
        .toList()

    fun encodeHistory(items: List<DevelopmentEvent>): String = items.joinToString("\n") { item ->
        listOf(
            item.id,
            item.title,
            item.domain.name,
            item.type.name,
            item.detail,
            item.occurredAt.toEpochMilli().toString()
        ).joinToString("|") { encode(it) }
    }

    fun decodeHistory(raw: String): List<DevelopmentEvent> = raw.lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            runCatching {
                val parts = line.split('|').map(::decode)
                require(parts.size == 6)
                DevelopmentEvent(
                    id = parts[0],
                    title = parts[1],
                    domain = DevelopmentDomain.valueOf(parts[2]),
                    type = EventType.valueOf(parts[3]),
                    detail = parts[4],
                    occurredAt = Instant.ofEpochMilli(parts[5].toLong())
                )
            }.getOrNull()
        }
        .toList()

    private fun encode(value: String): String =
        Base64.getEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String =
        String(Base64.getDecoder().decode(value), Charsets.UTF_8)
}
