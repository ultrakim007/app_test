package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.DailyCheckState
import com.example.data.MedicationSlot
import com.example.ui.components.MedicationCheckCard
import com.example.ui.components.WaterIntakeSection
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvoryBackgroundWarm
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurfaceVariant
import com.example.ui.theme.MedicineGreen
import com.example.ui.theme.MedicineGreenBorder
import com.example.ui.theme.MedicineGreenSoft

@Composable
fun DailyCheckScreen(
  viewModel: DailyCheckViewModel,
  modifier: Modifier = Modifier,
) {
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.refreshDateIfNeeded()
  }

  val context = LocalContext.current
  DisposableEffect(context, viewModel) {
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
          viewModel.refreshDateIfNeeded()
        }
      }
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    onDispose { context.unregisterReceiver(receiver) }
  }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  DailyCheckContent(
    state = uiState,
    onToggleMedication = viewModel::onToggleMedication,
    onAddWaterCup = viewModel::onAddWaterCup,
    onRemoveWaterCup = viewModel::onRemoveWaterCup,
    modifier = modifier,
  )
}

@Composable
fun DailyCheckContent(
  state: DailyCheckState,
  onToggleMedication: (MedicationSlot) -> Unit,
  onAddWaterCup: () -> Unit,
  onRemoveWaterCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = IvoryBackground,
  ) { innerPadding ->
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .drawBehind {
            drawRect(
              brush =
                Brush.verticalGradient(
                  colors = listOf(IvoryBackground, IvoryBackgroundWarm),
                )
            )
          }
          .padding(innerPadding)
          .verticalScroll(rememberScrollState()),
      contentAlignment = Alignment.TopCenter,
    ) {
      val isWideLayout = maxWidth >= 840.dp

      Column(
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = if (isWideLayout) 1040.dp else 640.dp)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        TodayDateHeaderCard(state = state)

        if (isWideLayout) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top,
          ) {
            MedicationCheckColumn(
              state = state,
              onToggleMedication = onToggleMedication,
              modifier = Modifier.weight(1f),
            )
            WaterIntakeSection(
              waterCups = state.waterCups,
              onAddCup = onAddWaterCup,
              onRemoveCup = onRemoveWaterCup,
              modifier = Modifier.weight(1f),
            )
          }
        } else {
          MedicationCheckColumn(
            state = state,
            onToggleMedication = onToggleMedication,
          )

          WaterIntakeSection(
            waterCups = state.waterCups,
            onAddCup = onAddWaterCup,
            onRemoveCup = onRemoveWaterCup,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun TodayDateHeaderCard(state: DailyCheckState) {
  val allMedsDone = state.isAllMedicationsTaken
  val badgeBg by
    animateColorAsState(
      targetValue = if (allMedsDone) MedicineGreenSoft else IvorySurfaceVariant,
      label = "summary_badge_bg",
    )
  val badgeContentColor by
    animateColorAsState(
      targetValue =
        if (allMedsDone) MedicineGreenBorder else MaterialTheme.colorScheme.onSurface,
      label = "summary_badge_text",
    )

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("date_header"),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(2.dp, IvoryBorder),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = null,
          tint = MedicineGreen,
          modifier = Modifier.size(30.dp),
        )
        Text(
          text = state.dayOfWeek,
          style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
          fontWeight = FontWeight.Bold,
          color = MedicineGreen,
        )
      }

      Text(
        text = state.formattedDate,
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 33.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = badgeBg,
        border = if (allMedsDone) BorderStroke(1.5.dp, MedicineGreen) else null,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector =
              if (allMedsDone) Icons.Rounded.CheckCircle else Icons.Rounded.Medication,
            contentDescription = null,
            tint = if (allMedsDone) MedicineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
          )
          Text(
            text =
              if (allMedsDone) stringResource(R.string.med_summary_all_done)
              else stringResource(R.string.med_summary_progress, 3, state.completedMedicationCount),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
            fontWeight = FontWeight.Bold,
            color = badgeContentColor,
          )
        }
      }
    }
  }
}

@Composable
private fun MedicationCheckColumn(
  state: DailyCheckState,
  onToggleMedication: (MedicationSlot) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 4.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Medication,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.size(28.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = stringResource(R.string.med_section_title),
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 25.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    MedicationCheckCard(
      slot = MedicationSlot.MORNING,
      isTaken = state.morningTaken,
      onClick = { onToggleMedication(MedicationSlot.MORNING) },
    )

    MedicationCheckCard(
      slot = MedicationSlot.LUNCH,
      isTaken = state.lunchTaken,
      onClick = { onToggleMedication(MedicationSlot.LUNCH) },
    )

    MedicationCheckCard(
      slot = MedicationSlot.DINNER,
      isTaken = state.dinnerTaken,
      onClick = { onToggleMedication(MedicationSlot.DINNER) },
    )
  }
}
