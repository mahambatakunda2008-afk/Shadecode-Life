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
import com.shadecode.life.core.model.DailyPlan
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentState

@Composable
fun StartingPointScreen(
    states: List<DevelopmentState>,
    nextFocus: DevelopmentState?,
    nextAction: DevelopmentAction?,
    dailyPlan: DailyPlan?,
    onStartAction: (DevelopmentAction) -> Unit,
    onViewHistory: () -> Unit,
    onViewGoals: () -> Unit,
    onViewCoach: () -> Unit,
    onExport: () -> Unit,
    onReset: () -> Unit
) {
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
