package com.startscope.coldstart.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startscope.coldstart.data.StartRepository
import com.startscope.coldstart.data.local.StartEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartDetailViewModel(
    private val repository: StartRepository,
    startId: Long,
) : ViewModel() {

    private val _start = MutableStateFlow<StartEntity?>(null)
    val start: StateFlow<StartEntity?> = _start.asStateFlow()

    init {
        viewModelScope.launch {
            _start.value = repository.getStart(startId)
        }
    }
}
