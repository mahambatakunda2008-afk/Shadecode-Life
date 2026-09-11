package com.shadecode.life.core.model

/** A concrete baseline task. Completion creates evidence for the personal model. */
data class BaselineItem(
    val id: String,
    val domain: DevelopmentDomain,
    val title: String,
    val prompt: String,
    val unit: String? = null,
    val targetDescription: String? = null
)

object BaselineCatalog {
    val items = listOf(
        BaselineItem(
            id = "body_pushups",
            domain = DevelopmentDomain.BODY,
            title = "Push-ups",
            prompt = "Do as many controlled push-ups as you can with good form. Stop when form breaks.",
            unit = "reps"
        ),
        BaselineItem(
            id = "body_plank",
            domain = DevelopmentDomain.BODY,
            title = "Plank",
            prompt = "Hold a comfortable plank with good form. Stop when you can no longer maintain it.",
            unit = "seconds"
        ),
        BaselineItem(
            id = "mind_explain",
            domain = DevelopmentDomain.MIND,
            title = "Explain something",
            prompt = "Explain a topic you know in simple language, as if teaching someone younger than you.",
            targetDescription = "Clear explanation with a beginning, middle, and end"
        ),
        BaselineItem(
            id = "communication_speak",
            domain = DevelopmentDomain.COMMUNICATION,
            title = "60-second explanation",
            prompt = "Speak for one minute about an idea without reading from a script.",
            targetDescription = "Clear, structured speech with minimal filler"
        ),
        BaselineItem(
            id = "discipline_focus",
            domain = DevelopmentDomain.DISCIPLINE,
            title = "Focused work block",
            prompt = "Choose one meaningful task and complete a distraction-free 25-minute block.",
            unit = "minutes"
        ),
        BaselineItem(
            id = "environment_reset",
            domain = DevelopmentDomain.ENVIRONMENT,
            title = "Reset your workspace",
            prompt = "Put your primary workspace into a state where you can start useful work immediately.",
            targetDescription = "Everything needed is accessible and unnecessary clutter is removed"
        ),
        BaselineItem(
            id = "finance_budget",
            domain = DevelopmentDomain.FINANCE,
            title = "Budget scenario",
            prompt = "Create a simple plan for dividing a fixed amount of money between needs, saving, and optional spending.",
            targetDescription = "Every amount has a reason and the total balances"
        ),
        BaselineItem(
            id = "capability_build",
            domain = DevelopmentDomain.CAPABILITY,
            title = "Build something",
            prompt = "Complete a small practical task that demonstrates one skill you want to develop.",
            targetDescription = "A real, observable result exists"
        )
    )
}
