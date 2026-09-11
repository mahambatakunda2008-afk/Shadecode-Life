# Shadecode Life

**A native personal development operating system.**

Shadecode Life is designed to help a person understand their current state, decide what matters next, act on it, measure real evidence, and adapt.

> Assess → Plan → Act → Measure → Reflect → Adapt

## Product thesis

This is not a generic habit tracker. The core question is:

**Given what we know about this person, what is the highest-leverage improvement they should make next?**

The app is local-first and privacy-first. Personal development data should remain on the device by default, with cloud sync treated as an explicit opt-in.

## Initial development domains

- Body: hygiene, fitness, nutrition, sleep
- Mind: learning, reasoning, knowledge
- Capability: technical and practical skills
- Communication: speaking, writing, listening
- Social: social intelligence and relationships
- Discipline: routines, attention, consistency
- Character: responsibility, integrity, self-management
- Finance: financial literacy and personal money management
- Career: goals, projects, entrepreneurship and professional development
- Environment: organization and personal systems
- Culture: general knowledge and cultural literacy

## Native stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- DataStore
- WorkManager
- Android Keystore / platform security APIs
- Optional Supabase sync later

No WebView. No PWA wrapper. The first target is a real Android application.

## Development principles

1. Real measurements over fake scores.
2. Evidence over streaks and gamification.
3. Local-first by default.
4. Recommendations must explain why they matter.
5. The product should become more useful as it learns the user's patterns.
6. AI is an assistant layer, not the source of truth.
7. Ship small, test with real use, then expand.

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

## Repository status

The repository is intentionally being built from a clean foundation. The first milestone is a compilable native Android shell with the domain architecture ready for the real product logic.

## License

MIT
