package com.shadecode.life.core.model

/** Explainable evaluation of how much one observation should influence the personal model. */
data class EvidenceAssessment(
    val evidence: Evidence,
    val relevance: EvidenceRelevance,
    val strength: EvidenceStrength,
    val recency: EvidenceRecency,
    val consistency: EvidenceConsistency,
    val impact: Double,
    val rationale: String
)

enum class EvidenceRelevance {
    UNLINKED,
    DOMAIN_RELEVANT,
    SKILL_SPECIFIC
}

enum class EvidenceStrength {
    SELF_REPORTED,
    OBSERVED_RESULT,
    REPEATED_RESULT,
    DEMONSTRATED
}

enum class EvidenceRecency {
    CURRENT,
    RECENT,
    AGING,
    OLD
}

enum class EvidenceConsistency {
    INSUFFICIENT,
    MIXED,
    CONSISTENT
}
