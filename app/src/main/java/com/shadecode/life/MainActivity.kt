package com.shadecode.life

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shadecode.life.action.ActionScreen
import com.shadecode.life.assessment.BaselineScreen
import com.shadecode.life.coach.CoachScreen
import com.shadecode.life.core.engine.CoachEngine
import com.shadecode.life.core.engine.DailyPlanner
import com.shadecode.life.core.engine.DevelopmentDecisionEngine
import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.engine.SkillEngine
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentGoal
import com.shadecode.life.core.model.GoalMilestone
import com.shadecode.life.core.model.SkillCatalog
import com.shadecode.life.core.state.DevelopmentSession
import com.shadecode.life.core.storage.LocalStateCodec
import com.shadecode.life.core.storage.LocalStateStore
import com.shadecode.life.dashboard.StartingPointScreen
import com.shadecode.life.development.DevelopmentMapScreen
import com.shadecode.life.goals.GoalScreen
import com.shadecode.life.timeline.DevelopmentTimelineScreen
import com.shadecode.life.ui.theme.ShadecodeLifeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadecodeLifeTheme {
                Surface(modifier = Modifier.fillMaxSize()) { LifeShell() }
            }
        }
    }
}

@Composable
private fun LifeShell() {
    val session = remember { DevelopmentSession() }
    val context = LocalContext.current
    val store = remember(context) { LocalStateStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var restored by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val page = remember { mutableStateOf(Page.WELCOME) }
    val activeAction = remember { mutableStateOf<DevelopmentAction?>(null) }
    val activeGoal = remember { mutableStateOf<DevelopmentGoal?>(null) }

    fun persist() {
        scope.launch {
            store.write(
                records = LocalStateCodec.encodeRecords(session.evidence()),
                history = LocalStateCodec.encodeHistory(session.events())
            )
        }
    }

    fun persistGoal(goal: DevelopmentGoal?) {
        scope.launch {
            store.write(
                records = LocalStateCodec.encodeRecords(session.evidence()),
                history = LocalStateCodec.encodeHistory(session.events()),
                goal = goal?.targetSkillId.orEmpty()
            )
        }
    }

    fun reset() {
        scope.launch {
            store.clear()
            session.replaceState(emptyList(), emptyList())
            activeAction.value = null
            activeGoal.value = null
            page.value = Page.WELCOME
        }
    }

    fun export() {
        val goal = activeGoal.value
        val evidence = session.evidence()
        val events = session.events()
        val body = buildString {
            appendLine("Shadecode Life development export")
            appendLine()
            goal?.let {
                appendLine("Goal: ${it.title}")
                appendLine("Domain: ${it.domain.title}")
                appendLine("Target skill: ${it.targetSkillId}")
                appendLine()
            }
            appendLine("Evidence (${evidence.size})")
            evidence.forEach { item ->
                appendLine("- ${item.title} | ${item.domain.title} | ${item.kind.name.lowercase()} | ${item.note.orEmpty()}")
            }
            appendLine()
            appendLine("Development history (${events.size})")
            events.forEach { event ->
                appendLine("- ${event.title} | ${event.domain.title} | ${event.type.name.lowercase()} | ${event.detail}")
            }
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Shadecode Life development export")
            putExtra(Intent.EXTRA_TEXT, body)
        }, "Export Shadecode Life"))
    }

    LaunchedEffect(store) {
        runCatching { store.read() }.getOrNull()?.let { saved ->
            val evidence = LocalStateCodec.decodeRecords(saved.records)
            val history = LocalStateCodec.decodeHistory(saved.history)
            session.replaceState(savedEvidence = evidence, savedEvents = history)
            saved.goal.takeIf { it.isNotBlank() }?.let { targetSkillId ->
                SkillCatalog.all.firstOrNull { it.id == targetSkillId }?.let { skill ->
                    activeGoal.value = buildGoal(skill.id, skill.title, skill.domain)
                }
            }
            if (evidence.isNotEmpty()) page.value = Page.START
        }
        restored = true
    }

    if (!restored) {
        WelcomeScreen(onBegin = {})
        return
    }

    when (page.value) {
        Page.WELCOME -> WelcomeScreen { page.value = Page.BASELINE }
        Page.BASELINE -> BaselineScreen(session) {
            persist()
            page.value = Page.START
        }
        Page.START -> {
            val states = session.states()
            val evidence = session.evidence()
            val dailyPlan = DailyPlanner.create(states)
            StartingPointScreen(
                states = states,
                nextFocus = session.nextFocus(),
                nextAction = DevelopmentEngine.recommendNextAction(states),
                dailyPlan = dailyPlan,
                evidence = evidence,
                onStartAction = {
                    activeAction.value = it
                    page.value = Page.ACTION
                },
                onViewHistory = { page.value = Page.TIMELINE },
                onViewGoals = { page.value = Page.GOALS },
                onViewCoach = { page.value = Page.COACH },
                onViewDevelopmentMap = { page.value = Page.DEVELOPMENT_MAP },
                onExport = ::export,
                onReset = { confirmReset = true }
            )
        }
        Page.ACTION -> activeAction.value?.let { action ->
            ActionScreen(
                action = action,
                onComplete = { reflection ->
                    session.recordAction(action, reflection)
                    persist()
                    activeAction.value = null
                    page.value = Page.START
                },
                onCancel = {
                    activeAction.value = null
                    page.value = Page.START
                }
            )
        }
        Page.TIMELINE -> DevelopmentTimelineScreen(
            events = session.events(),
            onBack = { page.value = Page.START }
        )
        Page.GOALS -> {
            val progress = SkillEngine.progress(session.evidence())
            val nextSkill = SkillEngine.nextSkill(session.evidence())
            val nextProgress = progress.firstOrNull { it.skill.id == nextSkill?.id }
            GoalScreen(
                goal = activeGoal.value,
                nextSkill = nextProgress,
                onCreateGoal = {
                    nextSkill?.let { skill ->
                        val goal = buildGoal(skill.id, skill.title, skill.domain)
                        activeGoal.value = goal
                        persistGoal(goal)
                    }
                },
                onBack = { page.value = Page.START }
            )
        }
        Page.COACH -> {
            val states = session.states()
            val progress = SkillEngine.progress(session.evidence())
            val insight = CoachEngine.generateInsight(session.evidence(), states, progress)
            CoachScreen(
                insight = insight,
                onTakeAction = {
                    insight.action?.let {
                        activeAction.value = it
                        page.value = Page.ACTION
                    }
                },
                onBack = { page.value = Page.START }
            )
        }
        Page.DEVELOPMENT_MAP -> {
            val progress = SkillEngine.progress(session.evidence())
            DevelopmentMapScreen(
                progress = progress,
                onBack = { page.value = Page.START }
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset development data?") },
            text = { Text("This permanently clears your local evidence and development history from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    reset()
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
}

private fun buildGoal(skillId: String, skillTitle: String, domain: com.shadecode.life.core.model.DevelopmentDomain): DevelopmentGoal =
    DevelopmentGoal(
        id = "goal_$skillId",
        title = "Build ${skillTitle.lowercase()}",
        domain = domain,
        description = "Develop this capability through repeated, observable evidence.",
        targetSkillId = skillId,
        milestones = listOf(
            GoalMilestone("${skillId}_foundation", "Collect first evidence", 1),
            GoalMilestone("${skillId}_functional", "Reach functional evidence", 3),
            GoalMilestone("${skillId}_reliable", "Build reliable evidence", 5),
            GoalMilestone("${skillId}_demonstrated", "Demonstrate the capability", 8)
        )
    )

private enum class Page { WELCOME, BASELINE, START, ACTION, TIMELINE, GOALS, COACH, DEVELOPMENT_MAP }

@Composable
private fun WelcomeScreen(onBegin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Shadecode Life", style = MaterialTheme.typography.headlineLarge)
        Text("Build yourself deliberately.", modifier = Modifier.padding(top = 12.dp, bottom = 24.dp))
        Button(onClick = onBegin) { Text("Begin") }
    }
}
