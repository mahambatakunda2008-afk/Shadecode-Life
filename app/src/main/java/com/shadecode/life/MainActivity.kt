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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadecode.life.assessment.BaselineScreen
import com.shadecode.life.ui.theme.ShadecodeLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadecodeLifeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LifeShell()
                }
            }
        }
    }
}

@Composable
private fun LifeShell() {
    var screen by remember { mutableStateOf(Screen.WELCOME) }

    when (screen) {
        Screen.WELCOME -> WelcomeScreen(onBegin = { screen = Screen.BASELINE })
        Screen.BASELINE -> BaselineScreen(onComplete = { screen = Screen.STARTING_POINT })
        Screen.STARTING_POINT -> StartingPointScreen()
    }
}

private enum class Screen {
    WELCOME,
    BASELINE,
    STARTING_POINT
}

@Composable
private fun WelcomeScreen(onBegin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Shadecode Life", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Build yourself deliberately.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
        )
        Button(onClick = onBegin) {
            Text("Begin baseline")
        }
    }
}

@Composable
private fun StartingPointScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your starting point", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Baseline evidence is captured. The next layer will turn it into a prioritized development plan.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
