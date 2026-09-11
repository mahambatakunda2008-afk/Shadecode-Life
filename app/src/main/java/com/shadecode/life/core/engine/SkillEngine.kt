package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStatus

/** Turns observed evidence into an explainable skill state. */
object SkillEngine {
    fun progress(evidence: List<Evidence>): List<SkillProgress> =
        SkillCatalog.all.map { skill ->
            val count = evidence.count { it.domain == skill.domain }
            SkillProgress(
                skill = skill,
                evidenceCount = count,
                status = statusFor(count)
            )
        }

    fun nextSkill(evidence: List<Evidence>): Skill? =
        progress(evidence)
            .filter { it.status != SkillStatus.DEMONSTRATED }
            .minWithOrNull(compareBy<SkillProgress>({ statusRank(it.status) }, { it.evidenceCount }))
            ?.skill

    private fun statusFor(evidenceCount: Int): SkillStatus =
        when {
            evidenceCount == 0 -> SkillStatus.NOT_STARTED
            evidenceCount < 3 -> SkillStatus.IN_PROGRESS
            else -> SkillStatus.DEMONSTRATED
        }

    private fun statusRank(status: SkillStatus): Int =
        when (status) {
            SkillStatus.NOT_STARTED -> 0
            SkillStatus.IN_PROGRESS -> 1
            SkillStatus.DEMONSTRATED -> 2
        }
}
