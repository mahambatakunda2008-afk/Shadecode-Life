package com.shadecode.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.action.ActionScreen
import com.shadecode.life.assessment.BaselineScreen
import com.shadecode.life.coach.CoachScreen
import com.shadecode.life.core.engine.CoachEngine
import com.shadecode.life.core.engine.DailyPlanner
import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.engine.SkillEngine
import com.shadecode.life.core.model.DevelopmentAction
import com.shadecode.life.core.model.DevelopmentGoal
import com.shadecode.life.core.model.GoalMilestone
import com.shadecode.life.core.state.DevelopmentSession
import com.shadecode.life.core.storage.LocalStateCodec
import com.shadecode.life.core.storage.LocalStateStore
import com.shadecode.life.dashboard.StartingPointScreen
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember(context) { LocalStateStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var restored by remember { mutableStateOf(false) }
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

    LaunchedEffect(store) {
        runCatching { store.read() }.getOrNull()?.let { saved ->
            session.replaceState(
                savedEvidence = LocalStateCodec.decodeRecords(saved.records),
                savedEvents = LocalStateCodec.decodeHistory(saved.history)
            )
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
            val dailyPlan = DailyPlanner.create(states)
            StartingPointScreen(
                states = states,
                nextFocus = session.nextFocus(),
                nextAction = DevelopmentEngine.recommendNextAction(states),
                dailyPlan = dailyPlan,
                onStartAction = {
                    activeAction.value = it
                    page.value = Page.ACTION
                },
                onViewHistory = { page.value = Page.TIMELINE },
                onViewGoals = { page.value = Page.GOALS },
                onViewCoach = { page.value = Page.COACH }
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
        Page.TIMELINE -> DevelopmentTimelineScreen(session.events())
        Page.GOALS -> {
            val progress = SkillEngine.progress(session.evidence())
            val nextSkill = SkillEngine.nextSkill(session.evidence())
            val nextProgress = progress.firstOrNull { it.skill.id == nextSkill?.id }

            GoalScreen(
                goal = activeGoal.value,
                nextSkill = nextProgress,
                onCreateGoal = {
                    nextSkill?.let { skill ->
                        activeGoal.value = DevelopmentGoal(
                            id = "goal_${skill.id}",
                            title = "Build ${skill.title.lowercase()}",
                            domain = skill.domain,
                            description = "Develop this capability through repeated, observable evidence.",
                            targetSkillId = skill.id,
                            milestones = listOf(
                                GoalMilestone("${skill.id}_foundation", "Collect first evidence", 1),
                                GoalMilestone("${skill.id}_functional", "Reach functional evidence", 3),
                                GoalMilestone("${skill.id}_reliable", "Build reliable evidence", 5),
                                GoalMilestone("${skill.id}_demonstrated", "Demonstrate the capability", 8)
                            )
                        )
                    }
                }
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
    }
}

private enum class Page { WELCOME, BASELINE, START, ACTION, TIMELINE, GOALS, COACH }

@Composable
private fun WelcomeScreen(onBegin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Text("Shadecode Life", style = MaterialTheme.typography.headlineLarge)
        androidx.compose.material3.Text("Build yourself deliberately.", modifier = Modifier.padding(top = 12.dp, bottom = 24.dp))
        Button(onClick = onBegin) { androidx.compose.material3.Text("Begin") }
    }
}
