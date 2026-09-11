package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

/** Turns observed evidence into progressive, prerequisite-aware skill state. */
object SkillEngine {
    fun progress(evidence: List<Evidence>): List<SkillProgress> =
        SkillCatalog.all.map { skill ->
            val count = evidence.count { item ->
                item.skillId == skill.id || (item.skillId == null && item.domain == skill.domain)
            }
            val prerequisitesMet = skill.prerequisites.all { prerequisiteId ->
                stageFor(
                    SkillCatalog.all.firstOrNull { it.id == prerequisiteId },
                    evidence
                ) >= SkillStage.FUNCTIONAL
            }
            val stage = stageFor(skill, evidence, prerequisitesMet)
            SkillProgress(
                skill = skill,
                evidenceCount = count,
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
        val count = evidence.count { item ->
            item.skillId == skill.id || (item.skillId == null && item.domain == skill.domain)
        }
        val prerequisitesMet = skill.prerequisites.all { prerequisiteId ->
            stageFor(SkillCatalog.all.firstOrNull { it.id == prerequisiteId }, evidence) >= SkillStage.FUNCTIONAL
        }
        return stageFor(skill, evidence, prerequisitesMet)
    }

    private fun stageFor(skill: Skill, evidence: List<Evidence>, prerequisitesMet: Boolean = true): SkillStage {
        val count = evidence.count { item ->
            item.skillId == skill.id || (item.skillId == null && item.domain == skill.domain)
        }
        val thresholds = skill.stageThresholds
        val rawStage = when {
            count >= thresholds.getOrElse(3) { 8 } -> SkillStage.DEMONSTRATED
            count >= thresholds.getOrElse(2) { 5 } -> SkillStage.RELIABLE
            count >= thresholds.getOrElse(1) { 3 } -> SkillStage.FUNCTIONAL
            count >= thresholds.getOrElse(0) { 1 } -> SkillStage.DEVELOPING
            else -> SkillStage.FOUNDATION
        }
        return if (rawStage == SkillStage.DEMONSTRATED && !prerequisitesMet) {
            SkillStage.RELIABLE
        } else {
            rawStage
        }
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
