package com.startscope.coldstart

import android.app.Application
import com.startscope.coldstart.data.StartRepository
import com.startscope.coldstart.data.local.AppDatabase
import com.startscope.coldstart.prefs.UserPreferences
import com.startscope.coldstart.work.WorkScheduling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class StartScopeApplication : Application() {

    lateinit var container: AppContainer
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WorkScheduling.schedulePeriodic(this)
    }
}

class AppContainer(app: StartScopeApplication) {
    private val database = AppDatabase.create(app)
    val userPreferences = UserPreferences(app)
    val repository = StartRepository(
        context = app,
        dao = database.startDao(),
        userPreferences = userPreferences,
    )
}
