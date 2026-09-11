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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.assessment.BaselineScreen
import com.shadecode.life.core.engine.DevelopmentEngine
import com.shadecode.life.core.state.DevelopmentSession
import com.shadecode.life.dashboard.StartingPointScreen
import com.shadecode.life.ui.theme.ShadecodeLifeTheme

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
    val page = remember { mutableStateOf(Page.WELCOME) }

    when (page.value) {
        Page.WELCOME -> WelcomeScreen { page.value = Page.BASELINE }
        Page.BASELINE -> BaselineScreen(session) { page.value = Page.START }
        Page.START -> StartingPointScreen(
            states = session.states(),
            nextFocus = session.nextFocus(),
            nextAction = DevelopmentEngine.recommendNextAction(session.states())
        )
    }
}

private enum class Page { WELCOME, BASELINE, START }

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
