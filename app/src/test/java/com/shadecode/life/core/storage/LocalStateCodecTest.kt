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
        val original = Evidence("action-body_capacity-1", DevelopmentDomain.BODY, "Repeat your physical baseline", "body_capacity", 12.5, "reps", "Controlled form", EvidenceKind.COMPLETED_TASK, Instant.parse("2026-09-13T18:00:00Z"), "skill-body_capacity-1")
        val decoded = LocalStateCodec.decodeRecords(LocalStateCodec.encodeRecords(listOf(original)))
        assertEquals(listOf(original), decoded)
    }

    @Test
    fun historyRoundTripPreservesActionAndReflectionEvents() {
        val original = listOf(
            DevelopmentEvent("action-1", "Repeat baseline", DevelopmentDomain.BODY, EventType.ACTION_COMPLETED, "12.0 reps", Instant.parse("2026-09-13T18:00:00Z")),
            DevelopmentEvent("reflection-1", "Reflection", DevelopmentDomain.BODY, EventType.REFLECTION, "Controlled form", Instant.parse("2026-09-13T18:00:00Z"))
        )
        assertEquals(original, LocalStateCodec.decodeHistory(LocalStateCodec.encodeHistory(original)))
    }

    @Test
    fun malformedRecordsAreIgnoredWithoutDiscardingValidRecords() {
        val valid = Evidence("valid", DevelopmentDomain.MIND, "Valid evidence", kind = EvidenceKind.OBSERVATION, recordedAt = Instant.parse("2026-09-13T18:00:00Z"))
        val decoded = LocalStateCodec.decodeRecords(LocalStateCodec.encodeRecords(listOf(valid)) + "\nnot-a-valid-record")
        assertEquals(listOf(valid), decoded)
        assertTrue(decoded.all { it.id == "valid" })
    }

    @Test
    fun legacyNineFieldEvidenceStillDecodesWithoutProvenance() {
        val original = Evidence("legacy", DevelopmentDomain.MIND, "Legacy evidence", "concept_explanation", null, null, "old", EvidenceKind.OBSERVATION, Instant.parse("2026-09-13T18:00:00Z"))
        val encoded = LocalStateCodec.encodeRecords(listOf(original))
        val legacy = encoded.substringBeforeLast("|")
        val decoded = LocalStateCodec.decodeRecords(legacy)
        assertEquals(original.copy(sourceActionId = null), decoded.single())
    }
}
