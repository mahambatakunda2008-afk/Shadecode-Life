package com.shadecode.life.goals

import androidx.compose.runtime.Composable
import com.shadecode.life.core.model.DevelopmentGoal
import com.shadecode.life.core.model.SkillProgress

@Composable
fun GoalScreen(
    goal: DevelopmentGoal?,
    nextSkill: SkillProgress?,
    onCreateGoal: () -> Unit
) {
    GoalScreen(
        goal = goal,
        nextSkill = nextSkill,
        onCreateGoal = onCreateGoal,
        onBack = {}
    )
}
