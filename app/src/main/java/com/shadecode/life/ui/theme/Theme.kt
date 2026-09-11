package com.shadecode.life.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LifeColors = lightColorScheme()

@Composable
fun ShadecodeLifeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeColors,
        content = content
    )
}
