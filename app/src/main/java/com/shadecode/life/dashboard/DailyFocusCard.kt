package com.shadecode.life.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.DailyPlan

@Composable
fun DailyFocusCard(
    plan: DailyPlan,
    onStart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Today's highest-leverage move", style = MaterialTheme.typography.labelLarge)
            Text(plan.action.title, style = MaterialTheme.typography.titleLarge)
            Text(plan.explanation, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${plan.action.estimatedMinutes} min • ${plan.focus.domain.title}",
                style = MaterialTheme.typography.labelMedium
            )
            Button(onClick = onStart) {
                Text("Start today's action")
            }
        }
    }
}
