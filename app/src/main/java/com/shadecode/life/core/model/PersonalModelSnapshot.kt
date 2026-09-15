package com.shadecode.life.core.model

/** A compact, derived view of the person's current development model. */
data class PersonalModelSnapshot(
    val evidenceCount: Int,
    val evidenceDays: Int,
    val domainsWithEvidence: Int,
    val domainCount: Int,
    val skillCount: Int,
    val startedSkillCount: Int,
    val demonstratedSkillCount: Int,
    val averageDomainConfidence: Double,
    val recentEventCount: Int,
    val nextSkillId: String?,
    val nextActionId: String?,
    val dataSufficiency: DataSufficiency
)

enum class DataSufficiency {
    EMPTY,
    SPARSE,
    DEVELOPING,
    ESTABLISHED
}
