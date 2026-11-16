package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MedicationSlot
import com.example.ui.theme.EveningIndigo
import com.example.ui.theme.EveningIndigoBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurfaceVariant
import com.example.ui.theme.LunchCoral
import com.example.ui.theme.LunchCoralBg
import com.example.ui.theme.MedicineGreen
import com.example.ui.theme.MedicineGreenBorder
import com.example.ui.theme.MorningAmber
import com.example.ui.theme.MorningAmberBg

@Composable
fun MedicationCheckCard(
  slot: MedicationSlot,
  isTaken: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  val title: String
  val subtitle: String
  val timeIcon: ImageVector
  val iconTint: Color
  val iconBg: Color
  val iconContentDescription: String
  val tagName: String

  when (slot) {
    MedicationSlot.MORNING -> {
      title = stringResource(R.string.med_morning_title)
      subtitle = stringResource(R.string.med_morning_subtitle)
      timeIcon = Icons.Rounded.WbSunny
      iconTint = MorningAmber
      iconBg = MorningAmberBg
      iconContentDescription = stringResource(R.string.cd_med_icon_morning)
      tagName = "med_morning_button"
    }
    MedicationSlot.LUNCH -> {
      title = stringResource(R.string.med_lunch_title)
      subtitle = stringResource(R.string.med_lunch_subtitle)
      timeIcon = Icons.Rounded.LightMode
      iconTint = LunchCoral
      iconBg = LunchCoralBg
      iconContentDescription = stringResource(R.string.cd_med_icon_lunch)
      tagName = "med_lunch_button"
    }
    MedicationSlot.DINNER -> {
      title = stringResource(R.string.med_dinner_title)
      subtitle = stringResource(R.string.med_dinner_subtitle)
      timeIcon = Icons.Rounded.NightsStay
      iconTint = EveningIndigo
      iconBg = EveningIndigoBg
      iconContentDescription = stringResource(R.string.cd_med_icon_dinner)
      tagName = "med_dinner_button"
    }
  }

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) MedicineGreen else MaterialTheme.colorScheme.surface,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "med_card_bg",
    )

  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) MedicineGreenBorder else IvoryBorder,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "med_card_border",
    )

  val primaryTextColor by
    animateColorAsState(
      targetValue = if (isTaken) Color.White else MaterialTheme.colorScheme.onSurface,
      label = "med_primary_text",
    )

  val secondaryTextColor by
    animateColorAsState(
      targetValue =
        if (isTaken) Color.White.copy(alpha = 0.92f)
        else MaterialTheme.colorScheme.onSurfaceVariant,
      label = "med_secondary_text",
    )

  val checkScale by
    animateFloatAsState(
      targetValue = if (isTaken) 1.0f else 0.96f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "med_check_scale",
    )

  val takenLabel = stringResource(R.string.med_status_taken)
  val pendingLabel = stringResource(R.string.med_status_pending)
  val checkedCd = stringResource(R.string.cd_med_checked)

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier =
      modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 108.dp)
        .minimumInteractiveComponentSize()
        .scale(checkScale)
        .testTag(tagName)
        .semantics {
          role = Role.Checkbox
          stateDescription = if (isTaken) takenLabel else pendingLabel
        },
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(2.dp, borderColor),
    shadowElevation = if (isTaken) 4.dp else 2.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left time-of-day icon badge
      Box(
        modifier =
          Modifier.size(60.dp)
            .clip(CircleShape)
            .background(if (isTaken) Color.White.copy(alpha = 0.22f) else iconBg),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = timeIcon,
          contentDescription = iconContentDescription,
          tint = if (isTaken) Color.White else iconTint,
          modifier = Modifier.size(34.dp),
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Middle title + meal hint
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 27.sp),
          fontWeight = FontWeight.Bold,
          color = primaryTextColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 22.sp),
          color = secondaryTextColor,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Right check status indicator
      AnimatedContent(
        targetState = isTaken,
        transitionSpec = {
          (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
            (fadeOut() + scaleOut(targetScale = 0.85f))
        },
        label = "med_status_badge",
      ) { checked ->
        if (checked) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
              Modifier.clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = checkedCd,
              tint = Color.White,
              modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = takenLabel,
              style = MaterialTheme.typography.labelLarge.copy(fontSize = 22.sp),
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
              Modifier.clip(RoundedCornerShape(18.dp))
                .background(IvorySurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
          ) {
            Box(
              modifier =
                Modifier.size(36.dp)
                  .border(width = 3.dp, color = IvoryBorder, shape = CircleShape)
                  .semantics { contentDescription = pendingLabel }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = pendingLabel,
              style = MaterialTheme.typography.labelMedium.copy(fontSize = 22.sp),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
