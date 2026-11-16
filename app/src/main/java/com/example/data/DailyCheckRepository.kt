package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

val Context.dailyCheckDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "daily_med_water_prefs")

enum class MedicationSlot {
  MORNING,
  LUNCH,
  DINNER,
}

data class TodayDateInfo(
  val dateKey: String,
  val formattedDate: String,
  val dayOfWeek: String,
)

data class DailyCheckState(
  val dateKey: String = "",
  val formattedDate: String = "",
  val dayOfWeek: String = "",
  val morningTaken: Boolean = false,
  val lunchTaken: Boolean = false,
  val dinnerTaken: Boolean = false,
  val waterCups: Int = 0,
) {
  val completedMedicationCount: Int
    get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

  val isAllMedicationsTaken: Boolean
    get() = morningTaken && lunchTaken && dinnerTaken
}

class DailyCheckRepository(
  private val dataStore: DataStore<Preferences>,
  private val dateProvider: () -> TodayDateInfo = ::currentTodayDateInfo,
) {
  private val currentDateTrigger = MutableStateFlow(dateProvider())

  private object Keys {
    val SAVED_DATE = stringPreferencesKey("saved_date")
    val MORNING_TAKEN = booleanPreferencesKey("morning_taken")
    val LUNCH_TAKEN = booleanPreferencesKey("lunch_taken")
    val DINNER_TAKEN = booleanPreferencesKey("dinner_taken")
    val WATER_CUPS = intPreferencesKey("water_cups")
  }

  val todayState: Flow<DailyCheckState> =
    combine(
      dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
      },
      currentDateTrigger,
    ) { prefs, today ->
      val savedDate = prefs[Keys.SAVED_DATE]
      if (savedDate == today.dateKey) {
        DailyCheckState(
          dateKey = today.dateKey,
          formattedDate = today.formattedDate,
          dayOfWeek = today.dayOfWeek,
          morningTaken = prefs[Keys.MORNING_TAKEN] ?: false,
          lunchTaken = prefs[Keys.LUNCH_TAKEN] ?: false,
          dinnerTaken = prefs[Keys.DINNER_TAKEN] ?: false,
          waterCups = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0),
        )
      } else {
        DailyCheckState(
          dateKey = today.dateKey,
          formattedDate = today.formattedDate,
          dayOfWeek = today.dayOfWeek,
          morningTaken = false,
          lunchTaken = false,
          dinnerTaken = false,
          waterCups = 0,
        )
      }
    }

  suspend fun refreshDateIfNeeded() {
    val today = dateProvider()
    currentDateTrigger.value = today
    dataStore.edit { prefs ->
      if (prefs[Keys.SAVED_DATE] != today.dateKey) {
        prefs[Keys.SAVED_DATE] = today.dateKey
        prefs[Keys.MORNING_TAKEN] = false
        prefs[Keys.LUNCH_TAKEN] = false
        prefs[Keys.DINNER_TAKEN] = false
        prefs[Keys.WATER_CUPS] = 0
      }
    }
  }

  suspend fun toggleMedication(slot: MedicationSlot) {
    val today = dateProvider()
    currentDateTrigger.value = today
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, today.dateKey)
      val key =
        when (slot) {
          MedicationSlot.MORNING -> Keys.MORNING_TAKEN
          MedicationSlot.LUNCH -> Keys.LUNCH_TAKEN
          MedicationSlot.DINNER -> Keys.DINNER_TAKEN
        }
      val current = prefs[key] ?: false
      prefs[key] = !current
    }
  }

  suspend fun incrementWater() {
    val today = dateProvider()
    currentDateTrigger.value = today
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, today.dateKey)
      val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
      prefs[Keys.WATER_CUPS] = current + 1
    }
  }

  suspend fun decrementWater() {
    val today = dateProvider()
    currentDateTrigger.value = today
    dataStore.edit { prefs ->
      ensureTodayDate(prefs, today.dateKey)
      val current = (prefs[Keys.WATER_CUPS] ?: 0).coerceAtLeast(0)
      if (current > 0) {
        prefs[Keys.WATER_CUPS] = current - 1
      }
    }
  }

  private fun ensureTodayDate(
    prefs: androidx.datastore.preferences.core.MutablePreferences,
    todayKey: String,
  ) {
    if (prefs[Keys.SAVED_DATE] != todayKey) {
      prefs[Keys.SAVED_DATE] = todayKey
      prefs[Keys.MORNING_TAKEN] = false
      prefs[Keys.LUNCH_TAKEN] = false
      prefs[Keys.DINNER_TAKEN] = false
      prefs[Keys.WATER_CUPS] = 0
    }
  }
}

fun currentTodayDateInfo(): TodayDateInfo {
  val now = Calendar.getInstance().time
  val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
  val displayFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
  val dayFormat = SimpleDateFormat("EEEE", Locale.KOREA)
  return TodayDateInfo(
    dateKey = keyFormat.format(now),
    formattedDate = displayFormat.format(now),
    dayOfWeek = dayFormat.format(now),
  )
}

fun millisUntilNextMidnight(nowMillis: Long = System.currentTimeMillis()): Long {
  val nextMidnight =
    Calendar.getInstance().apply {
      timeInMillis = nowMillis
      add(Calendar.DAY_OF_YEAR, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 100)
    }
  return (nextMidnight.timeInMillis - nowMillis).coerceAtLeast(1_000L)
}
