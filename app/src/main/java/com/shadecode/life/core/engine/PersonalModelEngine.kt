package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DataSufficiency
import com.shadecode.life.core.model.DevelopmentEvent
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.PersonalModelSnapshot
import com.shadecode.life.core.model.SkillStatus
import java.time.ZoneId

/** Builds the derived personal model used by product surfaces without storing a fake score. */
object PersonalModelEngine {
    fun snapshot(
        evidence: List<Evidence>,
        events: List<DevelopmentEvent> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): PersonalModelSnapshot {
        val progress = SkillEngine.progress(evidence)
        val states = DevelopmentEngine.buildStates(evidence)
        val evidenceDays = evidence.map { it.recordedAt.atZone(zoneId).toLocalDate() }.distinct().size
        val domainsWithEvidence = states.count { it.evidenceCount > 0 }
        val started = progress.count { it.status != SkillStatus.NOT_STARTED }
        val demonstrated = progress.count { it.status == SkillStatus.DEMONSTRATED }
        val averageConfidence = states.map { it.confidence }.average().takeIf { it.isFinite() } ?: 0.0
        val recentEvents = events.count { it.occurredAt != null }.coerceAtMost(10)
        val nextSkill = SkillEngine.nextSkill(evidence)
        val nextAction = DevelopmentDecisionEngine.nextAction(evidence)

        return PersonalModelSnapshot(
            evidenceCount = evidence.size,
            evidenceDays = evidenceDays,
            domainsWithEvidence = domainsWithEvidence,
            domainCount = states.size,
            skillCount = progress.size,
            startedSkillCount = started,
            demonstratedSkillCount = demonstrated,
            averageDomainConfidence = averageConfidence,
            recentEventCount = recentEvents,
            nextSkillId = nextSkill?.id,
            nextActionId = nextAction?.id,
            dataSufficiency = sufficiency(evidence.size, evidenceDays, domainsWithEvidence)
        )
    }

    private fun sufficiency(evidenceCount: Int, evidenceDays: Int, domainsWithEvidence: Int): DataSufficiency = when {
        evidenceCount == 0 -> DataSufficiency.EMPTY
        evidenceCount < 4 || evidenceDays < 2 -> DataSufficiency.SPARSE
        domainsWithEvidence < 3 -> DataSufficiency.DEVELOPING
        else -> DataSufficiency.ESTABLISHED
    }
}
