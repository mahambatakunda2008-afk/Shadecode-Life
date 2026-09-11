package com.shadecode.life.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.engine.DevelopmentDecisionEngine
import com.shadecode.life.core.model.DailyPlan
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentState
import com.shadecode.life.core.model.Evidence

@Composable
fun StartingPointScreen(
    states: List<DevelopmentState>,
    nextFocus: DevelopmentState?,
    nextAction: DevelopmentAction?,
    dailyPlan: DailyPlan?,
    evidence: List<Evidence> = emptyList(),
    onStartAction: (DevelopmentAction) -> Unit,
    onViewHistory: () -> Unit,
    onViewGoals: () -> Unit,
    onViewCoach: () -> Unit,
    onViewDevelopmentMap: () -> Unit = {},
    onExport: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    val decision = DevelopmentDecisionEngine.next(evidence)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your starting point", style = MaterialTheme.typography.headlineMedium)
        Text(
            "This is not a score for your worth. It is a map of what we know, what we do not know yet, and where to collect useful evidence next.",
            style = MaterialTheme.typography.bodyLarge
        )

        decision?.let { NextCapabilityCard(it) }
        dailyPlan?.let { plan -> DailyFocusCard(plan) { onStartAction(plan.action) } }
        nextAction?.let { action -> WhatNextCard(action, onStartAction) }

        nextFocus?.let { focus ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current focus", style = MaterialTheme.typography.labelLarge)
                    Text(focus.domain.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (focus.evidenceCount == 0) "There is no baseline evidence here yet."
                        else "This area currently has the least confidence in your model.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Text("Development map", style = MaterialTheme.typography.titleLarge)
        states.forEach { state -> DomainStateCard(state) }

        Button(onClick = onViewDevelopmentMap, modifier = Modifier.fillMaxWidth()) {
            Text("Open development map")
        }
        Button(onClick = onViewCoach, modifier = Modifier.fillMaxWidth()) { Text("Ask your coach") }
        Button(onClick = onViewGoals, modifier = Modifier.fillMaxWidth()) { Text("Goals & skills") }
        Button(onClick = onViewHistory, modifier = Modifier.fillMaxWidth()) { Text("View development history") }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onExport, modifier = Modifier.weight(1f)) { Text("Export") }
            Button(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset") }
        }
    }
}

@Composable
private fun NextCapabilityCard(decision: DevelopmentDecisionEngine.Decision) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Highest-leverage next capability", style = MaterialTheme.typography.labelLarge)
            Text(decision.skill.title, style = MaterialTheme.typography.headlineSmall)
            Text(decision.skill.description, style = MaterialTheme.typography.bodyLarge)
            Text(decision.reason, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${stageLabel(decision.progress.stage)} · ${decision.progress.evidenceCount} evidence item${if (decision.progress.evidenceCount == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun WhatNextCard(action: DevelopmentAction, onStartAction: (DevelopmentAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("What next?", style = MaterialTheme.typography.labelLarge)
            Text(action.title, style = MaterialTheme.typography.titleLarge)
            Text(action.reason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Text("${action.estimatedMinutes} min • ${action.domain.title}", style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = { onStartAction(action) }, modifier = Modifier.padding(top = 12.dp)) { Text("Start") }
        }
    }
}

@Composable
private fun DomainStateCard(state: DevelopmentState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(state.domain.title, style = MaterialTheme.typography.titleMedium)
            Text("${state.evidenceCount} evidence point${if (state.evidenceCount == 1) "" else "s"}", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { state.confidence.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }
    }
}

private fun stageLabel(stage: com.shadecode.life.core.model.SkillStage): String = when (stage) {
    com.shadecode.life.core.model.SkillStage.FOUNDATION -> "Foundation"
    com.shadecode.life.core.model.SkillStage.DEVELOPING -> "Developing"
    com.shadecode.life.core.model.SkillStage.FUNCTIONAL -> "Functional"
    com.shadecode.life.core.model.SkillStage.RELIABLE -> "Reliable"
    com.shadecode.life.core.model.SkillStage.DEMONSTRATED -> "Demonstrated"
}
