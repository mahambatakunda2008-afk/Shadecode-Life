package com.shadecode.life.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.lifeStateStore by preferencesDataStore(name = "shadecode_life_state")

/** Small on-device store. It deliberately has no network or account layer. */
class LocalStateStore(private val context: Context) {
    private val recordsKey = stringPreferencesKey("records_v1")
    private val historyKey = stringPreferencesKey("history_v1")

    suspend fun read(): LocalState {
        val values = context.lifeStateStore.data.first()
        return LocalState(
            records = values[recordsKey].orEmpty(),
            history = values[historyKey].orEmpty()
        )
    }

    suspend fun write(records: String, history: String) {
        context.lifeStateStore.edit { values ->
            values[recordsKey] = records
            values[historyKey] = history
        }
    }

    suspend fun clear() {
        context.lifeStateStore.edit { values ->
            values.remove(recordsKey)
            values.remove(historyKey)
        }
    }
}

data class LocalState(
    val records: String,
    val history: String
)
