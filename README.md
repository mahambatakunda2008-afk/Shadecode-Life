# Shadecode Life

**A native personal development operating system.**

Shadecode Life helps a person understand their current state, decide what matters next, act on it, measure real evidence, reflect, and adapt.

> Assess → Plan → Act → Measure → Reflect → Adapt

## Product thesis

This is not a generic habit tracker. The core question is:

**Given what we know about this person, what is the highest-leverage improvement they should make next?**

The product is local-first and privacy-first. Personal development data remains on the device by default. Cloud sync is an explicit opt-in.

## Core architecture

```text
Evidence → Skill progress → Trend → Decision → Action → Outcome → Evidence
```

The decision layer combines evidence sufficiency, development stage, prerequisites, recent activity, and measurable trends. Non-comparable capabilities are not forced into fake numeric scores.

## Development domains

- Body
- Mind
- Capability
- Communication
- Social
- Discipline
- Character
- Finance
- Career
- Environment
- Culture

## Native stack

- Kotlin
- Jetpack Compose
- Material 3
- DataStore for the current local state layer
- Android platform security APIs
- Optional Supabase sync later

No WebView. No PWA wrapper. The target is a real Android application.

## Current foundation

The repository now contains working foundations for:

- evidence-backed baseline state
- local persistence of evidence and development history
- skill progression with prerequisites
- unit-safe measurable trends
- evidence-aware, stage-aware and trend-aware decisions
- adaptive action generation
- structured action outcomes feeding back into evidence
- development-map trajectory views
- local-state codec round-trip tests
- closed-loop session tests
- CI JVM tests and debug APK builds

## Roadmap

### v0.1.0 · Personal Baseline
- First-run setup
- Development domains
- Evidence-based baseline assessments
- Personal profile/state model
- Initial priorities

### v0.2.0 · Daily Development
- Daily actions
- Goals
- Progress evidence
- Reflection
- Adaptive priorities

### v0.3.0 · Skill System
- Skills and competencies
- Prerequisites
- Skill tree
- Evidence-backed mastery

### v0.4.0 · Personal Coach
- Context-aware coaching
- Explainable recommendations
- Reflection analysis
- Planning support

### v0.5.0 · Device & Health Integration
- Optional Android health/device integrations
- Activity and sleep signals where available
- Strong privacy controls

### v1.0.0 · Public Release
- Stable core experience
- Export and backup
- Optional encrypted sync
- Production hardening

## Development principles

1. Real measurements over fake scores.
2. Evidence over streaks and gamification.
3. Local-first by default.
4. Recommendations must explain why they matter.
5. The product should become more useful as it learns the user's patterns.
6. AI is an assistant layer, not the source of truth.
7. Ship small, test with real use, then expand.

## License

MIT
