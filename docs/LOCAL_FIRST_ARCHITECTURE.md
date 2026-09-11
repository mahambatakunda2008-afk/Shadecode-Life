# Shadecode Life: local-first architecture

## Principle

Shadecode Life treats personal-development data as user-owned state. The default storage path is on-device. No account, network request, analytics backend, or remote AI call is required for the core development loop.

## Current storage layer

The app contains a compact local state codec and a DataStore-backed local store:

- `core/storage/LocalStateCodec.kt` serializes evidence and development history into compact line records.
- `core/storage/LocalStateStore.kt` persists those records with AndroidX DataStore Preferences.
- The storage dependency is `androidx.datastore:datastore-preferences`.

The codec uses Base64 for individual fields so user-entered text can safely contain separators and newlines without corrupting the record format.

## State model

The persisted state is intentionally small:

```text
records_v1  -> Evidence[]
history_v1  -> DevelopmentEvent[]
```

This is deliberately not a cloud database schema. It is an on-device snapshot format that can later be replaced or migrated without coupling the product to an account system.

## Next wiring step

The remaining step is to connect the store to the live `DevelopmentSession` lifecycle:

1. Read local state when the app starts.
2. Rehydrate the session before showing the development dashboard.
3. Persist after baseline completion.
4. Persist after an action/reflection is recorded.
5. Preserve recorded timestamps during rehydration.
6. Add an explicit local-data reset/export surface in Settings.

Until that wiring is merged, the current app session remains in memory even though the persistence foundation is present.

## Privacy boundary

Remote AI should be introduced only behind an explicit boundary. The local engine should continue to handle deterministic scoring, evidence weighting, trend detection, planning, and basic recommendations without sending personal-development records off-device.
