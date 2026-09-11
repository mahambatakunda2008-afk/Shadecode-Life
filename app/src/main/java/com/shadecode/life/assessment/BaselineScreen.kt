package com.shadecode.life.assessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.BaselineCatalog
import com.shadecode.life.core.model.BaselineItem

@Composable
fun BaselineScreen(
    onComplete: () -> Unit
) {
    val items = BaselineCatalog.items
    var index by remember { mutableIntStateOf(0) }
    var response by remember { mutableStateOf("") }
    var completed by remember { mutableStateOf(false) }

    if (completed) {
        BaselineComplete(onComplete = onComplete)
        return
    }

    val item = items[index]
    val progress = (index + 1).toFloat() / items.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personal baseline", style = MaterialTheme.typography.headlineMedium)
        Text(
            "We are collecting evidence, not grading who you are.",
            style = MaterialTheme.typography.bodyLarge
        )
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Text("${index + 1} of ${items.size}", style = MaterialTheme.typography.labelLarge)

        BaselineCard(item = item, response = response, onResponseChange = { response = it })

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (index > 0) {
                OutlinedButton(
                    onClick = {
                        index -= 1
                        response = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }

            Button(
                onClick = {
                    if (index == items.lastIndex) {
                        completed = true
                    } else {
                        index += 1
                        response = ""
                    }
                },
                enabled = response.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (index == items.lastIndex) "Finish baseline" else "Continue")
            }
        }
    }
}

@Composable
private fun BaselineCard(
    item: BaselineItem,
    response: String,
    onResponseChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(item.domain.title, style = MaterialTheme.typography.labelLarge)
            Text(item.title, style = MaterialTheme.typography.titleLarge)
            Text(item.prompt, style = MaterialTheme.typography.bodyLarge)

            item.targetDescription?.let {
                Text("Look for: $it", style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = response,
                onValueChange = onResponseChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(item.unit?.let { "Result ($it)" } ?: "What happened?") },
                minLines = 3
            )
        }
    }
}

@Composable
private fun BaselineComplete(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Baseline captured", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Your first data points are ready. Next, Shadecode Life can turn them into a development plan and choose a useful first focus.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
            Text("See my starting point")
        }
    }
}
