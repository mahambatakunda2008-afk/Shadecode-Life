package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
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

    /** Turns a selected capability into a concrete action the user can start now. */
    fun actionFor(decision: Decision): DevelopmentAction {
        val measuring = decision.progress.evidenceCount == 0
        val (title, minutes, kind) = when (decision.skill.id) {
            "body_capacity" -> if (measuring) Triple("Measure your current physical capacity", 5, ActionKind.MEASURE)
            else Triple("Complete a short physical capacity session", 15, ActionKind.PRACTICE)
            "wider_knowledge" -> if (measuring) Triple("Capture one thing you know and explain it", 5, ActionKind.MEASURE)
            else Triple("Learn one idea outside your usual specialization", 20, ActionKind.PRACTICE)
            "concept_explanation" -> if (measuring) Triple("Explain one concept in your own words", 5, ActionKind.MEASURE)
            else Triple("Explain one concept without looking at your notes", 10, ActionKind.PRACTICE)
            "focused_work" -> if (measuring) Triple("Run one focused work session", 20, ActionKind.MEASURE)
            else Triple("Complete one uninterrupted focused work block", 25, ActionKind.PRACTICE)
            "organized_workspace" -> if (measuring) Triple("Do a five-minute workspace reset", 5, ActionKind.MEASURE)
            else Triple("Reset the workspace you rely on most", 10, ActionKind.PRACTICE)
            "build_artifact" -> if (measuring) Triple("Define a tiny artifact you can finish", 10, ActionKind.MEASURE)
            else Triple("Build one small working artifact", 30, ActionKind.PRACTICE)
            "clear_speaking" -> if (measuring) Triple("Record a one-minute explanation", 5, ActionKind.MEASURE)
            else Triple("Record and review a one-minute explanation", 10, ActionKind.PRACTICE)
            "active_listening" -> if (measuring) Triple("Notice and record how you listen in one conversation", 5, ActionKind.MEASURE)
            else Triple("Have one conversation where you listen before responding", 15, ActionKind.PRACTICE)
            "keep_commitment" -> if (measuring) Triple("Choose one commitment you can keep today", 5, ActionKind.MEASURE)
            else Triple("Complete one commitment you deliberately made", 15, ActionKind.PRACTICE)
            "basic_budgeting" -> if (measuring) Triple("Record today's money position", 5, ActionKind.MEASURE)
            else Triple("Make a simple plan for your next spending decision", 15, ActionKind.PRACTICE)
            "opportunity_mapping" -> if (measuring) Triple("Map one opportunity and its requirements", 10, ActionKind.MEASURE)
            else Triple("Map one opportunity into concrete next steps", 20, ActionKind.PRACTICE)
            else -> if (measuring) Triple("Collect one useful observation", 5, ActionKind.MEASURE)
            else Triple("Practice this capability deliberately", 15, ActionKind.PRACTICE)
        }

        return DevelopmentAction(
            id = "develop_${decision.skill.id}_${kind.name.lowercase()}",
            domain = decision.skill.domain,
            title = title,
            reason = decision.reason,
            estimatedMinutes = minutes,
            kind = kind,
            skillId = decision.skill.id
        )
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
                "There is no evidence for this capability yet, and developing it can unlock $unlocks other capabilit${if (unlocks == 1) "y" else "ies"}."
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
