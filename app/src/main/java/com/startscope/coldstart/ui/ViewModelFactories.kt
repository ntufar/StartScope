package com.startscope.coldstart.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.startscope.coldstart.data.StartRepository
import com.startscope.coldstart.prefs.UserPreferences
import com.startscope.coldstart.ui.detail.AppDetailViewModel
import com.startscope.coldstart.ui.detail.StartDetailViewModel
import com.startscope.coldstart.ui.settings.SettingsViewModel
import com.startscope.coldstart.ui.timeline.TimelineViewModel

object ViewModelFactories {

    fun timeline(context: Context, repository: StartRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(TimelineViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return TimelineViewModel(
                    context.applicationContext as Application,
                    repository,
                ) as T
            }
        }

    fun appDetail(repository: StartRepository, packageName: String): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(AppDetailViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return AppDetailViewModel(repository, packageName) as T
            }
        }

    fun startDetail(repository: StartRepository, startId: Long): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(StartDetailViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return StartDetailViewModel(repository, startId) as T
            }
        }

    fun settings(preferences: UserPreferences): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(preferences) as T
            }
        }
}
