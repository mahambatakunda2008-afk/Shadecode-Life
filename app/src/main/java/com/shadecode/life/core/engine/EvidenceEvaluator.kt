package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceAssessment
import com.shadecode.life.core.model.EvidenceConsistency
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.EvidenceRecency
import com.shadecode.life.core.model.EvidenceRelevance
import com.shadecode.life.core.model.EvidenceStrength
import com.shadecode.life.core.model.SkillCatalog
import java.time.Duration
import java.time.Instant

/**
 * Deterministic evidence evaluator for v0.1.
 * It does not claim that an observation is objectively good or bad. It only estimates
 * how much weight the current model should give it based on explicit, inspectable signals.
 */
object EvidenceEvaluator {
    fun assessAll(evidence: List<Evidence>, now: Instant = Instant.now()): List<EvidenceAssessment> =
        evidence.map { assess(it, evidence, now) }

    fun assess(target: Evidence, allEvidence: List<Evidence>, now: Instant = Instant.now()): EvidenceAssessment {
        val skillSpecific = !target.skillId.isNullOrBlank() && SkillCatalog.all.any { it.id == target.skillId }
        val sameDomain = allEvidence.any { it.domain == target.domain }
        val sameSkill = target.skillId?.let { id -> allEvidence.count { it.skillId == id } } ?: 0

        val relevance = when {
            skillSpecific -> EvidenceRelevance.SKILL_SPECIFIC
            sameDomain -> EvidenceRelevance.DOMAIN_RELEVANT
            else -> EvidenceRelevance.UNLINKED
        }

        val strength = when {
            target.kind == EvidenceKind.ARTIFACT && sameSkill >= 4 -> EvidenceStrength.DEMONSTRATED
            target.kind == EvidenceKind.MEASUREMENT && sameSkill >= 3 -> EvidenceStrength.REPEATED_RESULT
            target.kind == EvidenceKind.COMPLETED_TASK && sameSkill >= 3 -> EvidenceStrength.REPEATED_RESULT
            target.kind == EvidenceKind.MEASUREMENT || target.kind == EvidenceKind.COMPLETED_TASK -> EvidenceStrength.OBSERVED_RESULT
            else -> EvidenceStrength.SELF_REPORTED
        }

        val ageDays = Duration.between(target.recordedAt, now).toDays().coerceAtLeast(0)
        val recency = when {
            ageDays <= 1 -> EvidenceRecency.CURRENT
            ageDays <= 7 -> EvidenceRecency.RECENT
            ageDays <= 30 -> EvidenceRecency.AGING
            else -> EvidenceRecency.OLD
        }

        val numericPeerValues = target.skillId?.let { id ->
            allEvidence.filter { it.skillId == id && it.value != null }.mapNotNull { it.value }
        }.orEmpty()
        val consistency = when {
            numericPeerValues.size < 2 -> EvidenceConsistency.INSUFFICIENT
            else -> {
                val average = numericPeerValues.average()
                val tolerance = (kotlin.math.abs(average) * 0.10).coerceAtLeast(0.5)
                val spread = numericPeerValues.maxOrNull()!! - numericPeerValues.minOrNull()!!
                if (spread <= tolerance) EvidenceConsistency.CONSISTENT else EvidenceConsistency.MIXED
            }
        }

        val impact = (
            relevanceWeight(relevance) *
                strengthWeight(strength) *
                recencyWeight(recency) *
                consistencyWeight(consistency)
            ).coerceIn(0.0, 1.0)

        val rationale = "${relevanceLabel(relevance)} evidence with ${strengthLabel(strength)} strength; " +
            "${recencyLabel(recency).lowercase()} and ${consistencyLabel(consistency).lowercase()} consistency."

        return EvidenceAssessment(target, relevance, strength, recency, consistency, impact, rationale)
    }

    private fun relevanceWeight(value: EvidenceRelevance) = when (value) {
        EvidenceRelevance.UNLINKED -> 0.35
        EvidenceRelevance.DOMAIN_RELEVANT -> 0.65
        EvidenceRelevance.SKILL_SPECIFIC -> 1.0
    }

    private fun strengthWeight(value: EvidenceStrength) = when (value) {
        EvidenceStrength.SELF_REPORTED -> 0.45
        EvidenceStrength.OBSERVED_RESULT -> 0.70
        EvidenceStrength.REPEATED_RESULT -> 0.90
        EvidenceStrength.DEMONSTRATED -> 1.0
    }

    private fun recencyWeight(value: EvidenceRecency) = when (value) {
        EvidenceRecency.CURRENT -> 1.0
        EvidenceRecency.RECENT -> 0.90
        EvidenceRecency.AGING -> 0.70
        EvidenceRecency.OLD -> 0.50
    }

    private fun consistencyWeight(value: EvidenceConsistency) = when (value) {
        EvidenceConsistency.INSUFFICIENT -> 0.75
        EvidenceConsistency.MIXED -> 0.80
        EvidenceConsistency.CONSISTENT -> 1.0
    }

    private fun relevanceLabel(value: EvidenceRelevance) = when (value) {
        EvidenceRelevance.UNLINKED -> "Unlinked"
        EvidenceRelevance.DOMAIN_RELEVANT -> "Domain-relevant"
        EvidenceRelevance.SKILL_SPECIFIC -> "Skill-specific"
    }

    private fun strengthLabel(value: EvidenceStrength) = when (value) {
        EvidenceStrength.SELF_REPORTED -> "self-reported"
        EvidenceStrength.OBSERVED_RESULT -> "single observed result"
        EvidenceStrength.REPEATED_RESULT -> "repeated result"
        EvidenceStrength.DEMONSTRATED -> "repeated artifact evidence"
    }

    private fun recencyLabel(value: EvidenceRecency) = when (value) {
        EvidenceRecency.CURRENT -> "Current"
        EvidenceRecency.RECENT -> "Recent"
        EvidenceRecency.AGING -> "Aging"
        EvidenceRecency.OLD -> "Old"
    }

    private fun consistencyLabel(value: EvidenceConsistency) = when (value) {
        EvidenceConsistency.INSUFFICIENT -> "insufficient data"
        EvidenceConsistency.MIXED -> "mixed results"
        EvidenceConsistency.CONSISTENT -> "consistent results"
    }
}
