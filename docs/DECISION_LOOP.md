# Shadecode Life decision loop

Shadecode Life is designed around a closed learning loop rather than a static task list:

```text
Evidence
   ↓
Skill progress
   ↓
Trend analysis
   ↓
Decision
   ↓
Concrete action
   ↓
Structured outcome
   ↓
New evidence
   └───────────────↺
```

## Evidence

Evidence is observable input to the personal model. It can be a measurement, completed task, artifact, communication event, reflection, or observation.

Evidence should retain its domain, optional skill target, value/unit when meaningful, note, kind, and timestamp. The system must not manufacture a numeric score for capabilities that cannot be measured safely or consistently.

## Trend

`ProgressTrendEngine` compares numeric evidence only when the metric direction and units make comparison meaningful. Current directions are:

- `IMPROVING`
- `DECLINING`
- `STABLE`
- `INSUFFICIENT_DATA`

For non-comparable capabilities, the absence of a numeric trend is intentional. Stage, evidence breadth, prerequisites, and recent activity remain available to the decision engine.

## Decision

`DevelopmentDecisionEngine` selects the next capability using:

- development stage gap
- evidence sufficiency
- breadth of evidence
- prerequisite unlock value
- recent trend

A declining measurable capability receives additional priority. Improving capabilities are not allowed to dominate the whole system simply because their number is rising.

## Action

The selected skill is converted into a concrete action appropriate to its stage:

- first evidence: measure
- foundation/developing: practice
- functional: apply in a real context
- reliable/demonstrated: stress-test or demonstrate

Recent action titles are considered so the system does not blindly repeat the same task when alternatives exist.

## Outcome

An action is completed with a structured `DevelopmentOutcome`. The outcome can contain:

- reflection
- numeric value
- unit

`DevelopmentSession.recordAction()` immediately turns that outcome into evidence and records action-completed and reflection events.

## Persistence

The evidence and event streams are encoded locally through `LocalStateCodec` and stored through `LocalStateStore`. This keeps the decision loop usable without a network dependency.

## Product rule

The loop is the product's core intelligence boundary. UI should present the result of the loop clearly, but should not invent progress, scores, trends, or recommendations independently of the underlying evidence model.
