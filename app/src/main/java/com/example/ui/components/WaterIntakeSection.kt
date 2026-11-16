package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MedicineGreen
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueDeep
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterCardBg
import com.example.ui.theme.WaterCardBorder
import com.example.ui.theme.WaterCupTrack
import kotlin.math.PI
import kotlin.math.sin

private const val DAILY_WATER_GOAL_CUPS = 8

@Composable
fun WaterIntakeSection(
  waterCups: Int,
  onAddCup: () -> Unit,
  onRemoveCup: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val goalReached = waterCups >= DAILY_WATER_GOAL_CUPS

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = WaterCardBg,
    border = BorderStroke(2.dp, WaterCardBorder),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      // Cup icon + large numeric count row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        AnimatedWaterCupIcon(
          waterCups = waterCups,
          goalCups = DAILY_WATER_GOAL_CUPS,
          modifier = Modifier.size(width = 96.dp, height = 112.dp),
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(R.string.water_section_title),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )

          AnimatedContent(
            targetState = waterCups,
            transitionSpec = {
              (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                (slideOutVertically { it / 2 } + fadeOut())
            },
            label = "water_cup_count_anim",
          ) { count ->
            Text(
              text = stringResource(R.string.water_cup_unit, count),
              style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
              fontWeight = FontWeight.Bold,
              color = WaterBlueDeep,
              modifier = Modifier.testTag("water_count_text"),
            )
          }

          Text(
            text =
              if (goalReached) stringResource(R.string.water_goal_reached)
              else stringResource(R.string.water_goal_status, waterCups),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 22.sp),
            fontWeight = if (goalReached) FontWeight.Bold else FontWeight.Medium,
            color = if (goalReached) MedicineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // 8-cup visual indicator strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        for (index in 1..DAILY_WATER_GOAL_CUPS) {
          val filled = index <= waterCups
          Box(
            modifier =
              Modifier.weight(1f)
                .height(14.dp)
                .clip(CircleShape)
                .background(if (filled) WaterBlue else WaterCupTrack)
          )
        }
      }

      // Primary "+1 cup" large action button
      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onAddCup()
        },
        modifier =
          Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 84.dp)
            .minimumInteractiveComponentSize()
            .testTag("add_water_button"),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(vertical = 8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.LocalDrink,
            contentDescription = stringResource(R.string.cd_water_add),
            modifier = Modifier.size(34.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = stringResource(R.string.water_add_button),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 23.sp),
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Accessible undo button when at least 1 cup has been logged
      if (waterCups > 0) {
        OutlinedButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRemoveCup()
          },
          modifier =
            Modifier.fillMaxWidth()
              .defaultMinSize(minHeight = 58.dp)
              .minimumInteractiveComponentSize()
              .testTag("undo_water_button"),
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, WaterCardBorder),
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White.copy(alpha = 0.7f),
              contentColor = WaterBlueDeep,
            ),
        ) {
          Icon(
            imageVector = Icons.Rounded.Remove,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.water_undo_button),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 22.sp),
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
fun AnimatedWaterCupIcon(
  waterCups: Int,
  goalCups: Int,
  modifier: Modifier = Modifier,
) {
  val targetProgress = (waterCups.coerceIn(0, goalCups).toFloat() / goalCups.toFloat())
  val animatedProgress by
    animateFloatAsState(
      targetValue = targetProgress,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
      label = "cup_fill_progress",
    )

  val infiniteTransition = rememberInfiniteTransition(label = "water_wave")
  val wavePhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2400, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "wave_phase",
    )

  val cupCd = stringResource(R.string.cd_water_cup)

  Box(modifier = modifier.semantics { contentDescription = cupCd }) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      val topLeftX = w * 0.12f
      val topRightX = w * 0.88f
      val bottomLeftX = w * 0.22f
      val bottomRightX = w * 0.78f
      val topY = h * 0.10f
      val bottomY = h * 0.90f
      val cupHeight = bottomY - topY

      val cupPath =
        Path().apply {
          moveTo(topLeftX, topY)
          lineTo(topRightX, topY)
          lineTo(bottomRightX, bottomY)
          quadraticTo(w * 0.50f, bottomY + h * 0.04f, bottomLeftX, bottomY)
          close()
        }

      // Soft empty cup interior
      drawPath(path = cupPath, color = Color.White.copy(alpha = 0.85f))

      // Animated rising water fill clipped inside the cup silhouette
      if (animatedProgress > 0.005f) {
        clipPath(cupPath) {
          val waterTopY = bottomY - (cupHeight * 0.88f * animatedProgress)
          val waveAmplitude = if (animatedProgress < 0.98f) h * 0.03f else h * 0.015f

          val waterPath =
            Path().apply {
              moveTo(0f, h)
              lineTo(0f, waterTopY)
              val steps = 24
              for (i in 0..steps) {
                val x = (i.toFloat() / steps) * w
                val yOffset = sin((i.toFloat() / steps) * 2f * PI.toFloat() + wavePhase) * waveAmplitude
                lineTo(x, waterTopY + yOffset)
              }
              lineTo(w, h)
              close()
            }

          drawPath(
            path = waterPath,
            brush =
              Brush.verticalGradient(
                colors = listOf(WaterBlueLight, WaterBlue),
                startY = waterTopY,
                endY = bottomY,
              ),
          )
        }
      }

      // Subtle cup measurement ticks on left side
      for (tick in 1..3) {
        val tickY = bottomY - (cupHeight * 0.22f * tick)
        val t = (tickY - topY) / cupHeight
        val leftEdgeX = topLeftX + (bottomLeftX - topLeftX) * (1f - (1f - t))
        drawLine(
          color = WaterCardBorder,
          start = Offset(leftEdgeX + w * 0.06f, tickY),
          end = Offset(leftEdgeX + w * 0.18f, tickY),
          strokeWidth = 3.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }

      // Crisp glass outline
      drawPath(
        path = cupPath,
        color = WaterBlueDeep,
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
      )

      // Rim highlight
      drawLine(
        color = WaterBlueDeep,
        start = Offset(topLeftX - w * 0.03f, topY),
        end = Offset(topRightX + w * 0.03f, topY),
        strokeWidth = 4.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
  }
}
