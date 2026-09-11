package com.shadecode.life.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.DevelopmentAction

@Composable
fun ActionScreen(
    action: DevelopmentAction,
    onComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var reflection by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Take action", style = MaterialTheme.typography.headlineMedium)
        Text(action.domain.title, style = MaterialTheme.typography.labelLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(action.title, style = MaterialTheme.typography.titleLarge)
                Text(action.reason, style = MaterialTheme.typography.bodyLarge)
                Text("Estimated time: ${action.estimatedMinutes} min", style = MaterialTheme.typography.labelMedium)
            }
        }

        OutlinedTextField(
            value = reflection,
            onValueChange = { reflection = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text("What happened?") },
            placeholder = { Text("Record what you actually did or learned.") }
        )

        Button(
            onClick = { onComplete(reflection.trim()) },
            enabled = reflection.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete action")
        }

        Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
