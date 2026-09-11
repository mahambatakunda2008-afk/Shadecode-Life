package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus
import java.time.ZoneOffset

/** Turns observed evidence into progressive, prerequisite-aware skill state. */
object SkillEngine {
    fun progress(evidence: List<Evidence>): List<SkillProgress> =
        SkillCatalog.all.map { skill ->
            val skillEvidence = evidenceFor(skill, evidence)
            val prerequisitesMet = skill.prerequisites.all { prerequisiteId ->
                stageFor(
                    SkillCatalog.all.firstOrNull { it.id == prerequisiteId },
                    evidence
                ) >= SkillStage.FUNCTIONAL
            }
            val stage = stageFor(skill, skillEvidence, prerequisitesMet)
            SkillProgress(
                skill = skill,
                evidenceCount = skillEvidence.size,
                stage = stage,
                status = statusFor(stage, prerequisitesMet),
                prerequisitesMet = prerequisitesMet
            )
        }

    fun nextSkill(evidence: List<Evidence>): Skill? =
        progress(evidence)
            .filter { it.status != SkillStatus.DEMONSTRATED && it.prerequisitesMet }
            .minWithOrNull(
                compareBy<SkillProgress>({ stageRank(it.stage) }, { it.evidenceCount }, { foundationRank(it.skill) })
            )
            ?.skill

    fun stageFor(skill: Skill?, evidence: List<Evidence>): SkillStage {
        if (skill == null) return SkillStage.FOUNDATION
        val skillEvidence = evidenceFor(skill, evidence)
        val prerequisitesMet = skill.prerequisites.all { prerequisiteId ->
            stageFor(SkillCatalog.all.firstOrNull { it.id == prerequisiteId }, evidence) >= SkillStage.FUNCTIONAL
        }
        return stageFor(skill, skillEvidence, prerequisitesMet)
    }

    private fun stageFor(skill: Skill, evidence: List<Evidence>, prerequisitesMet: Boolean = true): SkillStage {
        val thresholds = skill.stageThresholds
        val distinctDays = evidence.map { it.recordedAt.atZone(ZoneOffset.UTC).toLocalDate() }.distinct().size
        val rawStage = when {
            evidence.size >= thresholds.getOrElse(3) { 8 } && distinctDays >= 4 -> SkillStage.DEMONSTRATED
            evidence.size >= thresholds.getOrElse(2) { 5 } && distinctDays >= 3 -> SkillStage.RELIABLE
            evidence.size >= thresholds.getOrElse(1) { 3 } && distinctDays >= 2 -> SkillStage.FUNCTIONAL
            evidence.size >= thresholds.getOrElse(0) { 1 } -> SkillStage.DEVELOPING
            else -> SkillStage.FOUNDATION
        }
        return if (rawStage == SkillStage.DEMONSTRATED && !prerequisitesMet) {
            SkillStage.RELIABLE
        } else {
            rawStage
        }
    }

    private fun evidenceFor(skill: Skill, evidence: List<Evidence>): List<Evidence> =
        evidence.filter { item ->
            item.skillId == skill.id || (item.skillId == null && item.domain == skill.domain)
        }

    private fun statusFor(stage: SkillStage, prerequisitesMet: Boolean): SkillStatus = when {
        stage == SkillStage.DEMONSTRATED && prerequisitesMet -> SkillStatus.DEMONSTRATED
        stage == SkillStage.FOUNDATION -> SkillStatus.NOT_STARTED
        else -> SkillStatus.IN_PROGRESS
    }

    private fun stageRank(stage: SkillStage): Int = when (stage) {
        SkillStage.FOUNDATION -> 0
        SkillStage.DEVELOPING -> 1
        SkillStage.FUNCTIONAL -> 2
        SkillStage.RELIABLE -> 3
        SkillStage.DEMONSTRATED -> 4
    }

    private fun foundationRank(skill: Skill): Int = SkillCatalog.all.indexOf(skill)
}
