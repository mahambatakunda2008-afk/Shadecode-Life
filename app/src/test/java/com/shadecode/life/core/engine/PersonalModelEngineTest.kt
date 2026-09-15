package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DataSufficiency
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.time.Instant
import java.time.ZoneId

class PersonalModelEngineTest {
    @Test
    fun emptyModelIsExplicitlyEmptyButCanStillRecommendMeasurement() {
        val snapshot = PersonalModelEngine.snapshot(emptyList())

        assertEquals(DataSufficiency.EMPTY, snapshot.dataSufficiency)
        assertEquals(0, snapshot.evidenceCount)
        assertEquals(0, snapshot.domainsWithEvidence)
        assertNotNull(snapshot.nextSkillId)
        assertNotNull(snapshot.nextActionId)
    }

    @Test
    fun modelBecomesEstablishedAfterEvidenceSpansDaysAndDomains() {
        val evidence = listOf(
            evidence("body", DevelopmentDomain.BODY, "2026-09-10T10:00:00Z"),
            evidence("mind", DevelopmentDomain.MIND, "2026-09-11T10:00:00Z"),
            evidence("capability", DevelopmentDomain.CAPABILITY, "2026-09-11T11:00:00Z"),
            evidence("communication", DevelopmentDomain.COMMUNICATION, "2026-09-12T10:00:00Z")
        )

        val snapshot = PersonalModelEngine.snapshot(evidence, zoneId = ZoneId.of("Africa/Harare"))

        assertEquals(DataSufficiency.ESTABLISHED, snapshot.dataSufficiency)
        assertEquals(4, snapshot.evidenceCount)
        assertEquals(3, snapshot.evidenceDays)
        assertEquals(4, snapshot.domainsWithEvidence)
        assert(snapshot.domainCount >= 4)
        assertEquals(snapshot.skillCount, snapshot.startedSkillCount + (snapshot.skillCount - snapshot.startedSkillCount))
    }

    @Test
    fun evidenceNearMidnightUsesTheUsersZoneForDayCounting() {
        val evidence = listOf(
            evidence("late", DevelopmentDomain.BODY, "2026-09-10T21:30:00Z"),
            evidence("early", DevelopmentDomain.BODY, "2026-09-11T00:30:00Z")
        )

        val snapshot = PersonalModelEngine.snapshot(evidence, zoneId = ZoneId.of("Africa/Harare"))

        assertEquals(2, snapshot.evidenceDays)
    }

    private fun evidence(id: String, domain: DevelopmentDomain, recordedAt: String) = Evidence(
        id = id,
        domain = domain,
        title = id,
        kind = EvidenceKind.OBSERVATION,
        recordedAt = Instant.parse(recordedAt)
    )
}
