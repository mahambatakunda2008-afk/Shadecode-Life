package com.shadecode.life.core.engine

import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

/**
 * Chooses the next capability to develop from the current evidence graph.
 *
 * This is deliberately deterministic and explainable. It does not invent a
 * personality score or pretend that every capability has equal leverage.
 */
object DevelopmentDecisionEngine {
    data class Decision(
        val skill: Skill,
        val progress: SkillProgress,
        val reason: String,
        val priority: Double
    )

    fun next(evidence: List<Evidence>): Decision? {
        val progress = SkillEngine.progress(evidence)
        return progress
            .filter { it.status != SkillStatus.DEMONSTRATED && it.prerequisitesMet }
            .map { candidate ->
                Decision(
                    skill = candidate.skill,
                    progress = candidate,
                    reason = reasonFor(candidate, progress),
                    priority = priorityFor(candidate, progress)
                )
            }
            .maxWithOrNull(compareBy<Decision>({ it.priority }, { -stageRank(it.progress.stage) }, { -SkillCatalog.all.indexOf(it.skill) }))
    }

    private fun priorityFor(candidate: SkillProgress, all: List<SkillProgress>): Double {
        val stageGap = when (candidate.stage) {
            SkillStage.FOUNDATION -> 1.0
            SkillStage.DEVELOPING -> 0.82
            SkillStage.FUNCTIONAL -> 0.60
            SkillStage.RELIABLE -> 0.35
            SkillStage.DEMONSTRATED -> 0.0
        }
        val evidenceGap = (1.0 - (candidate.weightedEvidence / 8.0)).coerceIn(0.0, 1.0)
        val breadth = if (candidate.evidenceCount == 0) 0.25 else 0.0
        val unlockValue = all.count { it.skill.prerequisites.contains(candidate.skill.id) } * 0.15
        return stageGap * 0.45 + evidenceGap * 0.25 + breadth * 0.10 + unlockValue * 0.20
    }

    private fun reasonFor(candidate: SkillProgress, all: List<SkillProgress>): String {
        val unlocks = all.count { it.skill.prerequisites.contains(candidate.skill.id) }
        return when {
            candidate.evidenceCount == 0 && unlocks > 0 ->
                "There is no evidence for this capability yet, and developing it can unlock $unlocks other capability${if (unlocks == 1) "" else "ies"}."
            candidate.evidenceCount == 0 ->
                "There is no evidence for this capability yet. Measuring it gives your personal model a useful starting point."
            candidate.stage == SkillStage.DEVELOPING ->
                "You have started this capability. The highest-value move is to turn early evidence into repeatable practice."
            candidate.stage == SkillStage.FUNCTIONAL ->
                "This capability is functional. Repeated evidence can make it more reliable."
            candidate.stage == SkillStage.RELIABLE ->
                "This capability is becoming reliable. The next step is to demonstrate it in a meaningful situation."
            else ->
                "This is currently the strongest available capability to develop next."
        }
    }

    private fun stageRank(stage: SkillStage): Int = when (stage) {
        SkillStage.FOUNDATION -> 0
        SkillStage.DEVELOPING -> 1
        SkillStage.FUNCTIONAL -> 2
        SkillStage.RELIABLE -> 3
        SkillStage.DEMONSTRATED -> 4
    }
}
