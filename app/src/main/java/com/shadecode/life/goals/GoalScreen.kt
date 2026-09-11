package com.shadecode.life.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.DevelopmentGoal
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage
import com.shadecode.life.core.model.SkillStatus

@Composable
fun GoalScreen(
    goal: DevelopmentGoal?,
    nextSkill: SkillProgress?,
    onCreateGoal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Goals & skills", style = MaterialTheme.typography.headlineMedium)
        Text(
            "A goal points somewhere. A skill tells us what capability must change to get there.",
            style = MaterialTheme.typography.bodyLarge
        )

        if (goal == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No active goal", style = MaterialTheme.typography.titleLarge)
                    Text("Start with one concrete outcome instead of trying to improve everything at once.")
                    Button(onClick = onCreateGoal) { Text("Create a goal") }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active goal", style = MaterialTheme.typography.labelLarge)
                    Text(goal.title, style = MaterialTheme.typography.titleLarge)
                    Text(goal.description)
                    Text("Skill: ${goal.targetSkillId}", style = MaterialTheme.typography.labelMedium)
                }
            }

            goal.milestones.forEach { milestone ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                        Text("Evidence required: ${milestone.requiredEvidence}")
                        Text(if (milestone.completed) "Completed" else "In progress")
                    }
                }
            }
        }

        nextSkill?.let { progress ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Next capability", style = MaterialTheme.typography.labelLarge)
                    Text(progress.skill.title, style = MaterialTheme.typography.titleLarge)
                    Text(progress.skill.description)
                    Text("Stage: ${stageLabel(progress.stage)}")
                    Text("${progress.evidenceCount} evidence point${if (progress.evidenceCount == 1) "" else "s"} • ${statusLabel(progress.status)}")
                    if (progress.skill.prerequisites.isNotEmpty()) {
                        Text(
                            if (progress.prerequisitesMet) "Prerequisites met" else "Prerequisites still developing",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

private fun stageLabel(stage: SkillStage): String = when (stage) {
    SkillStage.FOUNDATION -> "Foundation"
    SkillStage.DEVELOPING -> "Developing"
    SkillStage.FUNCTIONAL -> "Functional"
    SkillStage.RELIABLE -> "Reliable"
    SkillStage.DEMONSTRATED -> "Demonstrated"
}

private fun statusLabel(status: SkillStatus): String =
    when (status) {
        SkillStatus.NOT_STARTED -> "Not started"
        SkillStatus.IN_PROGRESS -> "In progress"
        SkillStatus.DEMONSTRATED -> "Demonstrated"
    }
