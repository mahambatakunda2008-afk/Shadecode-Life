package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.SkillStage
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkillEngineTest {
    @Test
    fun dependentSkillCannotBeDemonstratedBeforePrerequisiteIsFunctional() {
        val evidence = (0 until 4).map { index ->
            Evidence(
                id = "concept-$index",
                domain = DevelopmentDomain.MIND,
                title = "Explain a concept $index",
                skillId = "concept_explanation",
                kind = EvidenceKind.COMPLETED_TASK,
                recordedAt = Instant.parse("2026-09-${10 + index}T10:00:00Z")
            )
        }
        val progress = SkillEngine.progress(evidence, ZoneId.of("Africa/Harare"))
        val dependent = progress.single { it.skill.id == "clear_speaking" }
        assertTrue(!dependent.prerequisitesMet)
        assertTrue(dependent.stage != SkillStage.DEMONSTRATED)
    }

    @Test
    fun prerequisiteUnlocksDependentSkillAfterItReachesFunctionalStage() {
        val evidence = (0 until 5).map { index ->
            Evidence(
                id = "knowledge-$index",
                domain = DevelopmentDomain.CULTURE,
                title = "Learn and explain idea $index",
                skillId = "wider_knowledge",
                kind = EvidenceKind.COMPLETED_TASK,
                recordedAt = Instant.parse("2026-09-${10 + (index % 3)}T10:00:00Z")
            )
        }
        val progress = SkillEngine.progress(evidence, ZoneId.of("Africa/Harare"))
        val prerequisite = progress.single { it.skill.id == "wider_knowledge" }
        val dependent = progress.single { it.skill.id == "concept_explanation" }
        assertTrue(prerequisite.stage >= SkillStage.FUNCTIONAL)
        assertTrue(dependent.prerequisitesMet)
    }

    @Test
    fun distinctEvidenceDaysRespectTheSuppliedTimezone() {
        val evidence = listOf(
            Evidence("boundary-1", DevelopmentDomain.BODY, "Night measurement", "body_capacity", 10.0, "reps", kind = EvidenceKind.MEASUREMENT, recordedAt = Instant.parse("2026-09-10T20:30:00Z")),
            Evidence("boundary-2", DevelopmentDomain.BODY, "Late measurement", "body_capacity", 11.0, "reps", kind = EvidenceKind.MEASUREMENT, recordedAt = Instant.parse("2026-09-10T21:30:00Z"))
        )
        val progress = SkillEngine.progress(evidence, ZoneId.of("Africa/Harare"))
        assertEquals(1, progress.single { it.skill.id == "body_capacity" }.distinctEvidenceDays)
    }
}
