package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

/** Chooses the next capability and a concrete action for developing it. */
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
                Decision(candidate.skill, candidate, reasonFor(candidate, progress), priorityFor(candidate, progress))
            }
            .maxWithOrNull(compareBy<Decision>({ it.priority }, { -stageRank(it.progress.stage) }, { -SkillCatalog.all.indexOf(it.skill) }))
    }

    fun nextAction(evidence: List<Evidence>): DevelopmentAction? = next(evidence)?.let(::actionFor)

    /** Turns the selected capability into a stage-appropriate action. */
    fun actionFor(decision: Decision): DevelopmentAction {
        val measuring = decision.progress.evidenceCount == 0
        val (title, minutes, kind) = when (decision.skill.id) {
            "body_capacity" -> if (measuring) Triple("Establish a physical baseline", 15, ActionKind.MEASURE) else Triple("Repeat your physical baseline", 15, ActionKind.PRACTICE)
            "wider_knowledge" -> Triple("Learn and explain one unfamiliar idea", 20, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            "concept_explanation" -> Triple("Explain one concept from memory", 15, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            "focused_work" -> Triple("Run one distraction-free work block", 25, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            "organized_workspace" -> Triple("Reset your working environment", 15, ActionKind.PRACTICE)
            "build_artifact" -> Triple("Build a small working artifact", 30, ActionKind.PRACTICE)
            "clear_speaking" -> Triple("Record a clear two-minute explanation", 15, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            "active_listening" -> Triple("Practice active listening", 15, ActionKind.PRACTICE)
            "keep_commitment" -> Triple("Keep one deliberate commitment", 20, ActionKind.PRACTICE)
            "basic_budgeting" -> Triple("Build a simple budget", 20, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            "opportunity_mapping" -> Triple("Map one real opportunity", 20, if (measuring) ActionKind.MEASURE else ActionKind.PRACTICE)
            else -> Triple("Practice ${decision.skill.title.lowercase()}", 15, ActionKind.PRACTICE)
        }
        val reason = when {
            measuring -> "This is a first measurement for ${decision.skill.title}. The goal is useful evidence, not a perfect performance."
            decision.progress.stage == SkillStage.DEVELOPING -> "You have started this capability. This action builds repeatable evidence instead of another checkbox."
            decision.progress.stage == SkillStage.FUNCTIONAL -> "This capability is functional. Repeating it deliberately tests whether it holds up consistently."
            decision.progress.stage == SkillStage.RELIABLE -> "This capability is becoming reliable. The next step is evidence in a meaningful situation."
            else -> decision.reason
        }
        return DevelopmentAction(
            id = "skill-${decision.skill.id}-${decision.progress.evidenceCount + 1}",
            domain = decision.skill.domain,
            title = title,
            reason = reason,
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
            candidate.evidenceCount == 0 && unlocks > 0 -> "There is no evidence for this capability yet, and developing it can unlock $unlocks other capabilit${if (unlocks == 1) "y" else "ies"}."
            candidate.evidenceCount == 0 -> "There is no evidence for this capability yet. Measuring it gives your personal model a useful starting point."
            candidate.stage == SkillStage.DEVELOPING -> "You have started this capability. The highest-value move is to turn early evidence into repeatable practice."
            candidate.stage == SkillStage.FUNCTIONAL -> "This capability is functional. Repeated evidence can make it more reliable."
            candidate.stage == SkillStage.RELIABLE -> "This capability is becoming reliable. The next step is to demonstrate it in a meaningful situation."
            else -> "This is currently the strongest available capability to develop next."
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
