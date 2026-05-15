package com.startscope.coldstart.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "startscope_prefs",
)

class UserPreferences(private val context: Context) {

    private val dataStore get() = context.dataStore

    private companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val KEY_RETENTION = intPreferencesKey("retention_days")
        val KEY_COLLECTION = booleanPreferencesKey("collection_enabled")
        const val DEFAULT_RETENTION = 7
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING] == true
    }

    val retentionDays: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[KEY_RETENTION] ?: DEFAULT_RETENTION).coerceIn(1, 30)
    }

    val collectionEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_COLLECTION] != false
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING] = value }
    }

    suspend fun setRetentionDays(days: Int) {
        dataStore.edit { it[KEY_RETENTION] = days.coerceIn(1, 30) }
    }

    suspend fun setCollectionEnabled(value: Boolean) {
        dataStore.edit { it[KEY_COLLECTION] = value }
    }

    suspend fun retentionDaysSnapshot(): Int = retentionDays.first()

}
