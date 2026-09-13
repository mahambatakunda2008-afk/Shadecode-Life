package com.shadecode.life.development

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.core.engine.ProgressTrendEngine
import com.shadecode.life.core.model.DevelopmentDomain
import com.shadecode.life.core.model.Evidence
import com.shadecode.life.core.model.SkillProgress
import com.shadecode.life.core.model.SkillStage

@Composable
fun DevelopmentMapScreen(
    progress: List<SkillProgress>,
    evidence: List<Evidence>,
    onBack: () -> Unit
) {
    var selectedSkillId by remember { mutableStateOf(progress.firstOrNull()?.skill?.id) }
    val selected = progress.firstOrNull { it.skill.id == selectedSkillId }
    val trends = remember(evidence, progress) {
        progress.associate { it.skill.id to ProgressTrendEngine.forSkill(it.skill, evidence) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your development map", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A living map of capabilities built from evidence. It is not a scorecard.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Button(onClick = onBack) { Text("Back") }
        }

        selected?.let { SkillDetailCard(it, trends[it.skill.id]) }

        DevelopmentDomain.entries.forEach { domain ->
            val domainSkills = progress.filter { it.skill.domain == domain }
            if (domainSkills.isNotEmpty()) {
                Text(domain.title, style = MaterialTheme.typography.titleLarge)
                Text(domain.description, style = MaterialTheme.typography.bodyMedium)
                domainSkills.forEach { skill ->
                    SkillMapCard(
                        progress = skill,
                        trend = trends[skill.skill.id],
                        selected = skill.skill.id == selectedSkillId,
                        onClick = { selectedSkillId = skill.skill.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillMapCard(
    progress: SkillProgress,
    trend: ProgressTrendEngine.Trend?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(progress.skill.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stageLabel(progress.stage), style = MaterialTheme.typography.labelLarge)
            }
            Text(progress.skill.description, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { stageProgress(progress.stage) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${progress.evidenceCount} evidence item${if (progress.evidenceCount == 1) "" else "s"} · " +
                    "${progress.distinctEvidenceDays} day${if (progress.distinctEvidenceDays == 1) "" else "s"} · " +
                    if (progress.prerequisitesMet) "prerequisites met" else "prerequisite work first",
                style = MaterialTheme.typography.labelMedium
            )
            trend?.let { TrendSummary(it) }
            if (selected) Text("Selected", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SkillDetailCard(progress: SkillProgress, trend: ProgressTrendEngine.Trend?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Current capability", style = MaterialTheme.typography.labelLarge)
            Text(progress.skill.title, style = MaterialTheme.typography.headlineSmall)
            Text(progress.skill.description, style = MaterialTheme.typography.bodyLarge)
            Text("Stage: ${stageLabel(progress.stage)}", style = MaterialTheme.typography.bodyMedium)
            Text("Weighted evidence: ${"%.1f".format(progress.weightedEvidence)}", style = MaterialTheme.typography.bodyMedium)
            Text("Evidence days: ${progress.distinctEvidenceDays}", style = MaterialTheme.typography.bodyMedium)
            trend?.let { TrendSummary(it, detailed = true) }
            Text(
                if (progress.prerequisitesMet) {
                    "The capability is available to develop now. Keep collecting real evidence rather than chasing a number."
                } else {
                    "A prerequisite capability is not functional yet. The next useful move is to build that foundation first."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TrendSummary(trend: ProgressTrendEngine.Trend, detailed: Boolean = false) {
    val latest = trend.latestValue
    val previous = trend.previousValue
    val change = trend.change
    val label = when (trend.direction) {
        ProgressTrendEngine.TrendDirection.IMPROVING -> "Improving"
        ProgressTrendEngine.TrendDirection.DECLINING -> "Declining"
        ProgressTrendEngine.TrendDirection.STABLE -> "Stable"
        ProgressTrendEngine.TrendDirection.INSUFFICIENT_DATA -> "More evidence needed"
    }
    val measurement = when {
        latest == null -> "No numeric measurements yet"
        previous == null -> "Latest measurement: ${formatNumber(latest)}"
        else -> "${formatNumber(previous)} → ${formatNumber(latest)} (${formatChange(change)})"
    }
    Text(
        if (detailed) "Trajectory: $label · $measurement" else "Trajectory: $label · $measurement",
        style = MaterialTheme.typography.labelMedium
    )
    if (detailed && trend.measuredCount >= 2) {
        Text(
            "Based on ${trend.measuredCount} numeric measurements. Trends describe the evidence; they do not assign a personal score.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value)

private fun formatChange(value: Double?): String = when {
    value == null -> ""
    value > 0 -> "+${formatNumber(value)}"
    else -> formatNumber(value)
}

private fun stageLabel(stage: SkillStage): String = when (stage) {
    SkillStage.FOUNDATION -> "Foundation"
    SkillStage.DEVELOPING -> "Developing"
    SkillStage.FUNCTIONAL -> "Functional"
    SkillStage.RELIABLE -> "Reliable"
    SkillStage.DEMONSTRATED -> "Demonstrated"
}

private fun stageProgress(stage: SkillStage): Float = when (stage) {
    SkillStage.FOUNDATION -> 0.1f
    SkillStage.DEVELOPING -> 0.3f
    SkillStage.FUNCTIONAL -> 0.55f
    SkillStage.RELIABLE -> 0.78f
    SkillStage.DEMONSTRATED -> 1f
}
