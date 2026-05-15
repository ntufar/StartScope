package com.startscope.coldstart.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startscope.coldstart.data.StartRepository
import com.startscope.coldstart.data.local.StartEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToLong

data class AppDetailUiState(
    val averages: StartRepository.TypeAverages,
    val reasonCounts: Map<Int, Int>,
    val recent: List<StartEntity>,
)

private fun computeState(list: List<StartEntity>): AppDetailUiState {
    fun avg(type: Int): Long? {
        val vals = list.filter {
            it.startType == type && it.ttidMs != null && it.ttidMs >= 0
        }.map { it.ttidMs!! }
        if (vals.isEmpty()) return null
        return vals.average().roundToLong()
    }
    return AppDetailUiState(
        averages = StartRepository.TypeAverages(
            coldMs = avg(android.app.ApplicationStartInfo.START_TYPE_COLD),
            warmMs = avg(android.app.ApplicationStartInfo.START_TYPE_WARM),
            hotMs = avg(android.app.ApplicationStartInfo.START_TYPE_HOT),
        ),
        reasonCounts = list.groupingBy { it.reason }.eachCount(),
        recent = list.take(20),
    )
}

class AppDetailViewModel(
    repository: StartRepository,
    packageName: String,
) : ViewModel() {

    private val sinceMs = System.currentTimeMillis() - 7L * 86_400_000L

    val state = repository.observeStartsForPackageSince(packageName, sinceMs)
        .map(::computeState)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            computeState(emptyList()),
        )
}
