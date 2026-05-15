package com.startscope.coldstart.ui.timeline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.startscope.coldstart.data.StartRepository
import com.startscope.coldstart.ui.DaySection
import com.startscope.coldstart.ui.groupStartsByDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TimelineViewModel(
    application: Application,
    private val repository: StartRepository,
) : AndroidViewModel(application) {

    private val _filter = MutableStateFlow<String?>(null)
    val filterPackage = _filter.asStateFlow()

    fun setFilterPackage(packageName: String?) {
        _filter.value = packageName
    }

    private val locale = application.resources.configuration.locales[0]

    val sections = combine(
        repository.observeStarts(),
        _filter,
    ) { all, filter ->
        val filtered = if (filter.isNullOrBlank()) {
            all
        } else {
            all.filter { it.packageName == filter }
        }
        groupStartsByDay(filtered, locale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val packages = repository.observeDistinctPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
