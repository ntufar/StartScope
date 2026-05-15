package com.startscope.coldstart.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startscope.coldstart.prefs.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferences,
) : ViewModel() {

    val retentionDays = preferences.retentionDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 7)

    val collectionEnabled = preferences.collectionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setRetentionDays(days: Int) {
        viewModelScope.launch {
            preferences.setRetentionDays(days)
        }
    }

    fun setCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setCollectionEnabled(enabled)
        }
    }
}
