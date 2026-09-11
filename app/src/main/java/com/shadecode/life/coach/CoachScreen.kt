package com.shadecode.life.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.CoachInsight

@Composable
fun CoachScreen(
    insight: CoachInsight,
    onTakeAction: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personal Coach", style = MaterialTheme.typography.headlineMedium)
        Text("A local, evidence-based recommendation. No pretending to know what the data does not show.")

        Card {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(insight.title, style = MaterialTheme.typography.titleLarge)
                Text(insight.message, style = MaterialTheme.typography.bodyLarge)
                Text("Why", style = MaterialTheme.typography.titleMedium)
                Text(insight.reason)
                Text("Evidence", style = MaterialTheme.typography.titleMedium)
                Text(insight.evidenceSummary)
            }
        }

        insight.action?.let { action ->
            Button(onClick = onTakeAction) {
                Text("Do: ${action.title}")
            }
        }

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
