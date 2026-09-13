package com.shadecode.life.core.storage

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentEvent
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.Instant

class LocalStateCodecTest {
    @Test
    fun evidenceRoundTripPreservesStructuredActionOutcome() {
        val original = Evidence(
            id = "action-body_capacity-1",
            domain = DevelopmentDomain.BODY,
            title = "Repeat your physical baseline",
            skillId = "body_capacity",
            value = 12.5,
            unit = "reps",
            note = "Controlled form",
            kind = EvidenceKind.COMPLETED_TASK,
            recordedAt = Instant.parse("2026-09-13T18:00:00Z")
        )

        val decoded = LocalStateCodec.decodeRecords(LocalStateCodec.encodeRecords(listOf(original)))

        assertEquals(listOf(original), decoded)
    }

    @Test
    fun historyRoundTripPreservesActionAndReflectionEvents() {
        val original = listOf(
            DevelopmentEvent(
                id = "action-1",
                title = "Repeat baseline",
                domain = DevelopmentDomain.BODY,
                type = EventType.ACTION_COMPLETED,
                detail = "12.0 reps",
                occurredAt = Instant.parse("2026-09-13T18:00:00Z")
            ),
            DevelopmentEvent(
                id = "reflection-1",
                title = "Reflection",
                domain = DevelopmentDomain.BODY,
                type = EventType.REFLECTION,
                detail = "Controlled form",
                occurredAt = Instant.parse("2026-09-13T18:00:00Z")
            )
        )

        val decoded = LocalStateCodec.decodeHistory(LocalStateCodec.encodeHistory(original))

        assertEquals(original, decoded)
    }

    @Test
    fun malformedRecordsAreIgnoredWithoutDiscardingValidRecords() {
        val valid = Evidence(
            id = "valid",
            domain = DevelopmentDomain.MIND,
            title = "Valid evidence",
            kind = EvidenceKind.OBSERVATION,
            recordedAt = Instant.parse("2026-09-13T18:00:00Z")
        )
        val raw = LocalStateCodec.encodeRecords(listOf(valid)) + "\nnot-a-valid-record"

        val decoded = LocalStateCodec.decodeRecords(raw)

        assertEquals(listOf(valid), decoded)
        assertTrue(decoded.all { it.id == "valid" })
    }
}
