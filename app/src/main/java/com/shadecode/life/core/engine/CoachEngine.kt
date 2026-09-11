package com.shadecode.life.core.engine

import com.shadecode.life.core.model.ActionKind
import com.shadecode.life.core.model.CoachInsight
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.Priority
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStatus

object CoachEngine {
    fun generateInsight(
        evidence: List<Evidence>,
        states: List<DevelopmentState>,
        skillProgress: List<SkillProgress>
    ): CoachInsight {
        val nextSkill = skillProgress
            .filter { it.status != SkillStatus.DEMONSTRATED }
            .minWithOrNull(compareBy<SkillProgress>({ statusRank(it.status) }, { it.evidenceCount }))

        val decliningState = states
            .filter { it.evidenceCount > 0 && it.trend == com.shadecode.life.core.model.Trend.DECLINING }
            .minByOrNull { it.confidence }

        if (decliningState != null) {
            val action = DevelopmentAction(
                id = "coach_stabilize_${decliningState.domain.name.lowercase()}",
                domain = decliningState.domain,
                title = "Stabilize your ${decliningState.domain.title.lowercase()}",
                reason = "Recent evidence suggests this area is declining. Stabilizing it comes before adding another goal.",
                estimatedMinutes = 15,
                kind = ActionKind.PRACTICE,
                skillId = skillProgress.firstOrNull { it.skill.domain == decliningState.domain }?.skill?.id
            )
            return CoachInsight(
                title = "Catch the decline",
                message = "Your ${decliningState.domain.title.lowercase()} is trending down.",
                reason = "The local trend engine found a meaningful decline across the available numeric evidence.",
                action = action,
                skill = skillProgress.firstOrNull { it.skill.domain == decliningState.domain },
                priority = Priority.HIGH,
                evidenceSummary = "Known: ${decliningState.evidenceCount} ${decliningState.domain.title.lowercase()} evidence item(s). Trend: declining."
            )
        }

        val unknownState = states
            .filter { it.evidenceCount == 0 }
            .minByOrNull { foundationRank(it.domain) }

        if (unknownState != null) {
            val skill = skillProgress.firstOrNull { it.skill.domain == unknownState.domain }
            val action = DevelopmentAction(
                id = "coach_measure_${unknownState.domain.name.lowercase()}",
                domain = unknownState.domain,
                title = "Measure ${unknownState.domain.title.lowercase()}",
                reason = "There is not enough evidence yet to know where improvement would have the most leverage.",
                estimatedMinutes = 5,
                kind = ActionKind.MEASURE,
                skillId = skill?.skill?.id
            )
            return CoachInsight(
                title = "Start with evidence",
                message = "Your next move is to measure ${unknownState.domain.title.lowercase()}.",
                reason = "This area has no recorded evidence, so a confident improvement decision would be guesswork.",
                action = action,
                skill = nextSkill,
                priority = Priority.HIGH,
                evidenceSummary = "Known: ${evidence.size} evidence item(s). Unknown: ${unknownState.domain.title}."
            )
        }

        if (nextSkill != null) {
            val action = DevelopmentAction(
                id = "coach_practice_${nextSkill.skill.id}",
                domain = nextSkill.skill.domain,
                title = "Practice ${nextSkill.skill.title.lowercase()}",
                reason = "Repeated evidence is needed to distinguish a one-off result from a developing capability.",
                estimatedMinutes = 15,
                kind = ActionKind.PRACTICE,
                skillId = nextSkill.skill.id
            )
            val statusText = when (nextSkill.status) {
                SkillStatus.NOT_STARTED -> "has not been started"
                SkillStatus.IN_PROGRESS -> "is developing"
                SkillStatus.DEMONSTRATED -> "is demonstrated"
            }
            return CoachInsight(
                title = "Build the capability",
                message = "${nextSkill.skill.title} $statusText. Create another piece of evidence.",
                reason = "The model currently has ${nextSkill.evidenceCount} evidence item(s) for this skill.",
                action = action,
                skill = nextSkill,
                priority = if (nextSkill.status == SkillStatus.NOT_STARTED) Priority.HIGH else Priority.MEDIUM,
                evidenceSummary = "Known: ${nextSkill.evidenceCount} evidence item(s) for ${nextSkill.skill.title}."
            )
        }

        return CoachInsight(
            title = "Keep observing",
            message = "Your current development map has enough evidence to keep tracking trends.",
            reason = "No immediate foundational gap was detected by the local decision engine.",
            action = null,
            skill = null,
            priority = Priority.LOW,
            evidenceSummary = "Known: ${evidence.size} evidence item(s). Inference: no urgent gap under current rules."
        )
    }

    private fun statusRank(status: SkillStatus): Int = when (status) {
        SkillStatus.NOT_STARTED -> 0
        SkillStatus.IN_PROGRESS -> 1
        SkillStatus.DEMONSTRATED -> 2
    }

    private fun foundationRank(domain: DevelopmentDomain): Int = when (domain) {
        DevelopmentDomain.BODY -> 0
        DevelopmentDomain.MIND -> 1
        DevelopmentDomain.DISCIPLINE -> 2
        DevelopmentDomain.ENVIRONMENT -> 3
        DevelopmentDomain.COMMUNICATION -> 4
        DevelopmentDomain.CAPABILITY -> 5
        DevelopmentDomain.FINANCE -> 6
        DevelopmentDomain.SOCIAL -> 7
        DevelopmentDomain.CHARACTER -> 8
        DevelopmentDomain.CAREER -> 9
        DevelopmentDomain.CULTURE -> 10
    }
}
