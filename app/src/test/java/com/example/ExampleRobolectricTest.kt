package com.example

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.DailyCheckRepository
import com.example.data.MedicationSlot
import com.example.data.TodayDateInfo
import com.example.data.millisUntilNextMidnight
import com.example.ui.DailyCheckScreen
import com.example.ui.DailyCheckViewModel
import com.example.ui.theme.SeniorCareTheme
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun `app name matches Korean title`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("오늘 약·물", appName)
  }

  @Test
  fun `repository persists medication and water and resets on new date`() = runTest {
    val prefsFile = File(tempFolder.newFolder(), "test_daily.preferences_pb")
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { prefsFile }

    var activeDate = TodayDateInfo("2026-11-15", "2026년 11월 15일", "일요일")
    val repository = DailyCheckRepository(dataStore) { activeDate }

    repository.refreshDateIfNeeded()
    repository.toggleMedication(MedicationSlot.MORNING)
    repository.incrementWater()
    repository.incrementWater()

    val sameDay = repository.todayState.first()
    assertTrue(sameDay.morningTaken)
    assertFalse(sameDay.lunchTaken)
    assertEquals(2, sameDay.waterCups)

    // Simulate new day -> automatic reset
    activeDate = TodayDateInfo("2026-11-16", "2026년 11월 16일", "월요일")
    repository.refreshDateIfNeeded()

    val nextDay = repository.todayState.first()
    assertEquals("2026년 11월 16일", nextDay.formattedDate)
    assertFalse(nextDay.morningTaken)
    assertEquals(0, nextDay.waterCups)
  }

  @Test
  fun `tapping medication and water buttons updates UI state`() {
    val prefsFile = File(tempFolder.newFolder(), "ui_test_daily.preferences_pb")
    val dataStore =
      PreferenceDataStoreFactory.create(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
      ) {
        prefsFile
      }
    val repository =
      DailyCheckRepository(dataStore) {
        TodayDateInfo("2026-11-15", "2026년 11월 15일", "일요일")
      }
    val viewModel = DailyCheckViewModel(repository)

    composeTestRule.setContent {
      SeniorCareTheme { DailyCheckScreen(viewModel = viewModel) }
    }

    composeTestRule.onNodeWithTag("date_header").assertIsDisplayed()
    composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("1잔").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("water_count_text").assertTextContains("1잔")
  }

  @Test
  fun `millisUntilNextMidnight computes duration to 00_00_00_100 next day`() {
    val cal =
      Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 15, 23, 59, 50)
        set(Calendar.MILLISECOND, 0)
      }
    val remaining = millisUntilNextMidnight(cal.timeInMillis)
    assertEquals(10_100L, remaining)
  }

  @Test
  fun `midnight rollover automatically resets medication and water while screen is open`() {
    val prefsFile = File(tempFolder.newFolder(), "midnight_test_daily.preferences_pb")
    val dataStore =
      PreferenceDataStoreFactory.create(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
      ) {
        prefsFile
      }
    var activeDate = TodayDateInfo("2026-11-15", "2026년 11월 15일", "일요일")
    val repository = DailyCheckRepository(dataStore) { activeDate }
    val viewModel = DailyCheckViewModel(repository, millisUntilMidnightProvider = { 1_000L })

    composeTestRule.setContent {
      SeniorCareTheme { DailyCheckScreen(viewModel = viewModel) }
    }

    composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isNotEmpty() &&
        composeTestRule.onAllNodesWithText("1잔").fetchSemanticsNodes().isNotEmpty()
    }

    // Roll date past midnight while the screen remains open and advance main looper past delay
    activeDate = TodayDateInfo("2026-11-16", "2026년 11월 16일", "월요일")
    shadowOf(Looper.getMainLooper()).idleFor(1_500, TimeUnit.MILLISECONDS)
    composeTestRule.mainClock.advanceTimeBy(1_500)

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("2026년 11월 16일").fetchSemanticsNodes().isNotEmpty() &&
        composeTestRule.onAllNodesWithText("0잔").fetchSemanticsNodes().isNotEmpty() &&
        composeTestRule.onAllNodesWithText("복용 완료").fetchSemanticsNodes().isEmpty()
    }

    // Also verify system ACTION_DATE_CHANGED broadcast triggers immediate reset on next day
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("1잔").fetchSemanticsNodes().isNotEmpty()
    }
    activeDate = TodayDateInfo("2026-11-17", "2026년 11월 17일", "화요일")
    ApplicationProvider.getApplicationContext<Context>().sendBroadcast(Intent(Intent.ACTION_DATE_CHANGED))
    shadowOf(Looper.getMainLooper()).idle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("2026년 11월 17일").fetchSemanticsNodes().isNotEmpty() &&
        composeTestRule.onAllNodesWithText("0잔").fetchSemanticsNodes().isNotEmpty()
    }
  }
}
