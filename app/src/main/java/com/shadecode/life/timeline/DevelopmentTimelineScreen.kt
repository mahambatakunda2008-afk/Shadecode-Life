package com.shadecode.life.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.model.DevelopmentEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DevelopmentTimelineScreen(
    events: List<DevelopmentEvent>,
    onBack: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d MMM, HH:mm")
        .withZone(ZoneId.systemDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Your development", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A record of the actions, measurements, and reflections that are shaping your personal model.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.padding(top = 12.dp)
                ) { Text("Back") }
            }
        }

        if (events.isEmpty()) {
            item {
                Text("Nothing recorded yet. Your first baseline will appear here.")
            }
        }

        items(events, key = { it.id }) { event ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text("${event.domain.title} • ${event.type.name.lowercase().replace('_', ' ')}", style = MaterialTheme.typography.labelMedium)
                    Text(event.detail, style = MaterialTheme.typography.bodyMedium)
                    Text(formatter.format(event.occurredAt), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
