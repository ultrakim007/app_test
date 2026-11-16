package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyCheckRepository
import com.example.data.DailyCheckState
import com.example.data.MedicationSlot
import com.example.data.currentTodayDateInfo
import com.example.data.millisUntilNextMidnight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DailyCheckViewModel(
  private val repository: DailyCheckRepository,
  private val millisUntilMidnightProvider: () -> Long = { millisUntilNextMidnight() },
) : ViewModel() {

  private val initialDate = currentTodayDateInfo()
  private var midnightWatcherJob: Job? = null

  val uiState: StateFlow<DailyCheckState> =
    repository.todayState.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue =
        DailyCheckState(
          dateKey = initialDate.dateKey,
          formattedDate = initialDate.formattedDate,
          dayOfWeek = initialDate.dayOfWeek,
        ),
    )

  init {
    refreshDateIfNeeded()
  }

  fun refreshDateIfNeeded() {
    viewModelScope.launch { repository.refreshDateIfNeeded() }
    scheduleMidnightWatcher()
  }

  private fun scheduleMidnightWatcher() {
    midnightWatcherJob?.cancel()
    midnightWatcherJob =
      viewModelScope.launch {
        while (isActive) {
          val waitMillis = millisUntilMidnightProvider().coerceAtLeast(1_000L)
          delay(waitMillis)
          repository.refreshDateIfNeeded()
        }
      }
  }

  fun onToggleMedication(slot: MedicationSlot) {
    viewModelScope.launch { repository.toggleMedication(slot) }
  }

  fun onAddWaterCup() {
    viewModelScope.launch { repository.incrementWater() }
  }

  fun onRemoveWaterCup() {
    viewModelScope.launch { repository.decrementWater() }
  }

  companion object {
    fun factory(repository: DailyCheckRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return DailyCheckViewModel(repository) as T
        }
      }
  }
}
