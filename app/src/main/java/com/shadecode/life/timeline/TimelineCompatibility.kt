package com.shadecode.life.timeline

import androidx.compose.runtime.Composable
import com.shadecode.life.core.model.DevelopmentEvent

@Composable
fun DevelopmentTimelineScreen(events: List<DevelopmentEvent>) {
    DevelopmentTimelineScreen(events = events, onBack = {})
}
