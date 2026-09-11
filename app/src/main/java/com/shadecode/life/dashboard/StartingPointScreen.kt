package com.shadecode.life.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.DevelopmentState

@Composable
fun StartingPointScreen(
    states: List<DevelopmentState>,
    nextFocus: DevelopmentState?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your starting point", style = MaterialTheme.typography.headlineMedium)
        Text(
            "This is not a score for your worth. It is a map of what we know, what we do not know yet, and where to collect useful evidence next.",
            style = MaterialTheme.typography.bodyLarge
        )

        nextFocus?.let { focus ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("First focus", style = MaterialTheme.typography.labelLarge)
                    Text(focus.domain.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (focus.evidenceCount == 0) {
                            "There is no baseline evidence here yet. Start by measuring it."
                        } else {
                            "This area currently has the least confidence in your model."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Text("Development map", style = MaterialTheme.typography.titleLarge)

        states.forEach { state ->
            DomainStateCard(state)
        }
    }
}

@Composable
private fun DomainStateCard(state: DevelopmentState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(state.domain.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "${state.evidenceCount} evidence point${if (state.evidenceCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { state.confidence.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
        }
    }
}
