package com.shadecode.life.core.engine

import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.EvidenceKind
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

/**
 * Chooses the next capability and turns that decision into a concrete action.
 *
 * The decision is deterministic and explainable. Action selection is based on
 * the capability stage and existing evidence rather than a generic task pool.
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

    fun nextAction(evidence: List<Evidence>): DevelopmentAction? =
        next(evidence)?.let { actionFor(it, evidence) }

    fun actionFor(decision: Decision, evidence: List<Evidence>): DevelopmentAction {
        val skill = decision.skill
        val prior = evidence.filter { it.skillId == skill.id }
        val hasMeasuredEvidence = prior.any { it.kind == EvidenceKind.MEASUREMENT }
        val action = when (skill.id) {
            "body_capacity" -> when (decision.progress.stage) {
                SkillStage.FOUNDATION -> ActionSpec("Establish a physical baseline", "Choose push-ups, a plank, and a 10-minute walk. Record honest results without trying to impress the app.", 15)
                else -> ActionSpec("Repeat your physical baseline", "Repeat the same simple measures under similar conditions. Compare with your previous evidence.", 15)
            }
            "wider_knowledge" -> ActionSpec("Learn and explain one unfamiliar idea", "Spend focused time learning one idea outside your usual work, then explain it in your own words.", 20)
            "concept_explanation" -> ActionSpec("Explain one concept from memory", "Pick something you know, explain it without notes, then check for gaps and correct them.", 15)
            "focused_work" -> ActionSpec("Run one distraction-free work block", "Choose one meaningful task, remove avoidable interruptions, and work on it continuously until the block ends.", 25)
            "organized_workspace" -> ActionSpec("Reset your working environment", "Make the space and tools you use for important work immediately usable. Remove only what blocks tomorrow's work.", 15)
            "build_artifact" -> ActionSpec("Build a small working artifact", "Turn one small idea into something functional. Keep the scope narrow enough to finish and demonstrate.", 30)
            "clear_speaking" -> ActionSpec("Record a clear two-minute explanation", "Explain one idea aloud with a beginning, middle, and end. Listen back and note one clarity improvement.", 15)
            "active_listening" -> ActionSpec("Practice active listening", "Have one conversation where you focus on understanding first. Summarize the other person's point before giving your response.", 15)
            "keep_commitment" -> ActionSpec("Keep one deliberate commitment", "Choose one promise you made to yourself or someone else. Complete it today and record what helped or got in the way.", 20)
            "basic_budgeting" -> ActionSpec("Build a simple budget", "List expected money in and out for a short period. Identify one decision that protects your future options.", 20)
            "opportunity_mapping" -> ActionSpec("Map one real opportunity", "Choose an opportunity and write down the requirement, current gap, next action, and evidence that would show progress.", 20)
            else -> ActionSpec("Practice ${skill.title.lowercase()}", "Do one deliberate practice session for this capability and record what happened.", 15)
        }
        val reason = when {
            !hasMeasuredEvidence && decision.progress.evidenceCount == 0 ->
                "This is a first measurement for ${skill.title}. The goal is useful evidence, not a perfect performance."
            decision.progress.stage == SkillStage.DEVELOPING ->
                "You have started this capability. This action increases repeatable evidence instead of just adding another checkbox."
            decision.progress.stage == SkillStage.FUNCTIONAL ->
                "This capability is functional. Repeating it in a deliberate setting helps test whether it holds up consistently."
            decision.progress.stage == SkillStage.RELIABLE ->
                "This capability is becoming reliable. The action now asks for evidence in a meaningful situation."
            else -> decision.reason
        }
        return DevelopmentAction(
            id = "skill-${skill.id}-${decision.progress.evidenceCount + 1}",
            title = action.title,
            reason = reason,
            domain = skill.domain,
            estimatedMinutes = action.minutes,
            targetSkillId = skill.id
        )
    }

    private data class ActionSpec(val title: String, val reason: String, val minutes: Int)

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
