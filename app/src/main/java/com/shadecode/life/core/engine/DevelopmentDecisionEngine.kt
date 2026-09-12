package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Skill
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

/** Chooses the next capability and an evidence-aware action for developing it. */
object DevelopmentDecisionEngine {
    data class Decision(
        val skill: Skill,
        val progress: SkillProgress,
        val reason: String,
        val priority: Double,
        val recentEvidenceTitles: List<String> = emptyList()
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
                    priority = priorityFor(candidate, progress),
                    recentEvidenceTitles = recentEvidenceTitles(candidate.skill.id, evidence)
                )
            }
            .maxWithOrNull(
                compareBy<Decision>({ it.priority }, { -stageRank(it.progress.stage) }, { -SkillCatalog.all.indexOf(it.skill) })
            )
    }

    fun nextAction(evidence: List<Evidence>): DevelopmentAction? = next(evidence)?.let(::actionFor)

    /** Turns the selected capability into a stage-appropriate action that changes with evidence. */
    fun actionFor(decision: Decision): DevelopmentAction {
        val firstMeasurement = decision.progress.evidenceCount == 0
        val stage = decision.progress.stage
        val recentTitles = decision.recentEvidenceTitles

        val action = when (decision.skill.id) {
            "body_capacity" -> bodyAction(stage, firstMeasurement, recentTitles)
            "wider_knowledge" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Learn and explain one unfamiliar idea"),
                practice = listOf("Learn and explain a second unfamiliar idea", "Explain an unfamiliar idea without notes"),
                application = listOf("Use a new idea to solve a real problem", "Compare two ideas and explain which is more useful"),
                demonstration = listOf("Teach an unfamiliar idea to someone else", "Explain an unfamiliar idea and answer follow-up questions"),
                minutes = 20
            )
            "concept_explanation" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Explain one concept from memory"),
                practice = listOf("Explain one concept without notes", "Explain the same concept using a concrete example"),
                application = listOf("Explain a concept while solving a real problem", "Connect one concept to another idea you already know"),
                demonstration = listOf("Teach one concept to a beginner", "Explain a concept and handle two follow-up questions"),
                minutes = 15
            )
            "focused_work" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Run one distraction-free work block"),
                practice = listOf("Run a 30-minute distraction-free work block", "Complete a focused block with your phone out of reach"),
                application = listOf("Finish a meaningful task in one focused block", "Recover from one interruption and finish the planned task"),
                demonstration = listOf("Complete a demanding 45-minute focused block", "Run a focused block on a task you have been avoiding"),
                minutes = 30
            )
            "organized_workspace" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Reset your working environment"),
                practice = listOf("Reset your workspace and keep it clear through one work session", "Organize the tools you use most often"),
                application = listOf("Prepare your workspace for a demanding task before starting", "Remove one recurring source of friction from your environment"),
                demonstration = listOf("Design a workspace setup that supports a full day of work", "Maintain an organized workspace through a demanding work session"),
                minutes = 15
            )
            "build_artifact" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Build a small working artifact"),
                practice = listOf("Build a second small artifact with one new constraint", "Improve a small artifact by handling one edge case"),
                application = listOf("Build something that solves a real problem for you", "Add a useful feature based on an actual need"),
                demonstration = listOf("Let someone use your artifact and respond to their feedback", "Demonstrate your artifact and explain the decisions behind it"),
                minutes = 30
            )
            "clear_speaking" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Record a clear two-minute explanation"),
                practice = listOf("Explain an idea clearly without notes", "Record a two-minute explanation with a deliberate structure"),
                application = listOf("Explain an idea to someone who is unfamiliar with it", "Give a concise explanation in a real conversation"),
                demonstration = listOf("Teach an idea and answer follow-up questions", "Give a three-minute explanation and handle interruptions calmly"),
                minutes = 15
            )
            "active_listening" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Practice active listening"),
                practice = listOf("Paraphrase what someone said before responding", "Ask one clarifying question before giving your view"),
                application = listOf("Summarize another person's position fairly", "Use active listening in a conversation where you disagree"),
                demonstration = listOf("Help someone feel understood in a difficult conversation", "Summarize a complex conversation and confirm your understanding"),
                minutes = 15
            )
            "keep_commitment" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Keep one deliberate commitment"),
                practice = listOf("Keep one small commitment exactly when promised", "Make one realistic commitment and complete it without reminders"),
                application = listOf("Keep a meaningful commitment despite an inconvenience", "Complete a commitment that requires sustained effort"),
                demonstration = listOf("Take ownership of a commitment that affects someone else", "Demonstrate reliability on a commitment with a real consequence"),
                minutes = 20
            )
            "basic_budgeting" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Build a simple budget"),
                practice = listOf("Build a budget for a different month or scenario", "Review a budget and identify one avoidable leak"),
                application = listOf("Make a real spending plan for the next week", "Plan for an unexpected expense without breaking the budget"),
                demonstration = listOf("Build a realistic monthly plan with trade-offs", "Explain a budget decision and its consequences"),
                minutes = 20
            )
            "opportunity_mapping" -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Map one real opportunity"),
                practice = listOf("Map a second opportunity and its requirements", "Compare two opportunities using explicit criteria"),
                application = listOf("Choose one opportunity and take its first real step", "Contact or research a real opportunity and record what you learn"),
                demonstration = listOf("Explain a complete opportunity path from entry to outcome", "Help someone else evaluate an opportunity using your map"),
                minutes = 20
            )
            else -> stagedAction(
                stage,
                firstMeasurement,
                recentTitles,
                baseline = listOf("Measure ${decision.skill.title.lowercase()}"),
                practice = listOf("Practice ${decision.skill.title.lowercase()} in a new context"),
                application = listOf("Apply ${decision.skill.title.lowercase()} to a real problem"),
                demonstration = listOf("Demonstrate ${decision.skill.title.lowercase()} in a meaningful situation"),
                minutes = 15
            )
        }

        val reason = when {
            firstMeasurement -> "This is a first measurement for ${decision.skill.title}. The goal is useful evidence, not a perfect performance."
            stage == SkillStage.DEVELOPING -> "You have started this capability. The next action deliberately varies the practice so your evidence is not just repetition."
            stage == SkillStage.FUNCTIONAL -> "This capability is functional. The next step moves it into a real context where it has to work outside practice."
            stage == SkillStage.RELIABLE -> "This capability is becoming reliable. The next step is to stress-test or demonstrate it in a meaningful situation."
            else -> decision.reason
        }

        return DevelopmentAction(
            id = "skill-${decision.skill.id}-${decision.progress.evidenceCount + 1}",
            domain = decision.skill.domain,
            title = action.title,
            reason = reason,
            estimatedMinutes = action.minutes,
            kind = action.kind,
            skillId = decision.skill.id
        )
    }

    private data class ActionChoice(val title: String, val minutes: Int, val kind: ActionKind)

    private fun stagedAction(
        stage: SkillStage,
        firstMeasurement: Boolean,
        recentTitles: List<String>,
        baseline: List<String>,
        practice: List<String>,
        application: List<String>,
        demonstration: List<String>,
        minutes: Int
    ): ActionChoice {
        if (firstMeasurement) return ActionChoice(baseline.first(), minutes, ActionKind.MEASURE)
        val (variants, kind) = when (stage) {
            SkillStage.FOUNDATION -> practice to ActionKind.PRACTICE
            SkillStage.DEVELOPING -> practice to ActionKind.PRACTICE
            SkillStage.FUNCTIONAL -> application to ActionKind.PRACTICE
            SkillStage.RELIABLE, SkillStage.DEMONSTRATED -> demonstration to ActionKind.PRACTICE
        }
        return ActionChoice(selectUnseen(variants, recentTitles), minutes, kind)
    }

    private fun bodyAction(stage: SkillStage, firstMeasurement: Boolean, recentTitles: List<String>): ActionChoice {
        if (firstMeasurement) return ActionChoice("Establish a physical baseline", 15, ActionKind.MEASURE)
        val variants = when (stage) {
            SkillStage.FOUNDATION, SkillStage.DEVELOPING -> listOf(
                "Repeat your physical baseline with strict form",
                "Repeat your physical baseline after a full rest day"
            )
            SkillStage.FUNCTIONAL -> listOf(
                "Apply your physical capacity in a longer movement session",
                "Test your physical capacity in a different movement pattern"
            )
            SkillStage.RELIABLE, SkillStage.DEMONSTRATED -> listOf(
                "Stress-test your physical capacity safely and record the result",
                "Demonstrate your physical capacity through a controlled challenge"
            )
        }
        return ActionChoice(selectUnseen(variants, recentTitles), 15, ActionKind.PRACTICE)
    }

    private fun selectUnseen(variants: List<String>, recentTitles: List<String>): String {
        return variants.firstOrNull { candidate ->
            recentTitles.none { it.equals(candidate, ignoreCase = true) }
        } ?: variants[recentTitles.size % variants.size]
    }

    private fun recentEvidenceTitles(skillId: String, evidence: List<Evidence>): List<String> =
        evidence
            .filter { it.skillId == skillId }
            .sortedByDescending { it.recordedAt }
            .take(4)
            .map { it.title }

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
