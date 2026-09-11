package com.shadecode.life.core.model

/** Foundational skill graph for v0.3. Prerequisites make progression directional without turning it into a game. */
object SkillCatalog {
    val all: List<Skill> = listOf(
        Skill(
            "body_capacity",
            "Basic physical capacity",
            DevelopmentDomain.BODY,
            "Build and measure a sustainable physical foundation."
        ),
        Skill(
            "wider_knowledge",
            "Build wider knowledge",
            DevelopmentDomain.CULTURE,
            "Learn and explain ideas beyond your immediate specialization."
        ),
        Skill(
            "concept_explanation",
            "Explain a concept",
            DevelopmentDomain.MIND,
            "Explain something accurately in your own words.",
            prerequisites = listOf("wider_knowledge")
        ),
        Skill(
            "focused_work",
            "Work with focus",
            DevelopmentDomain.DISCIPLINE,
            "Complete deliberate work with fewer avoidable interruptions."
        ),
        Skill(
            "organized_workspace",
            "Maintain an organized environment",
            DevelopmentDomain.ENVIRONMENT,
            "Keep the space and tools needed for work usable."
        ),
        Skill(
            "build_artifact",
            "Build a small artifact",
            DevelopmentDomain.CAPABILITY,
            "Turn an idea into something functional or useful.",
            prerequisites = listOf("focused_work")
        ),
        Skill(
            "clear_speaking",
            "Speak clearly",
            DevelopmentDomain.COMMUNICATION,
            "Communicate an idea with structure, clarity, and purpose.",
            prerequisites = listOf("concept_explanation")
        ),
        Skill(
            "active_listening",
            "Listen actively",
            DevelopmentDomain.SOCIAL,
            "Understand another person before responding.",
            prerequisites = listOf("clear_speaking")
        ),
        Skill(
            "keep_commitment",
            "Keep a commitment",
            DevelopmentDomain.CHARACTER,
            "Do what you deliberately said you would do.",
            prerequisites = listOf("focused_work")
        ),
        Skill(
            "basic_budgeting",
            "Manage a simple budget",
            DevelopmentDomain.FINANCE,
            "Track money decisions and make a realistic plan."
        ),
        Skill(
            "opportunity_mapping",
            "Map opportunities",
            DevelopmentDomain.CAREER,
            "Identify useful paths, requirements, and next steps.",
            prerequisites = listOf("basic_budgeting", "clear_speaking")
        )
    )
}
