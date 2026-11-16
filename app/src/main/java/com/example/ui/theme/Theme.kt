package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SeniorIvoryColorScheme =
  lightColorScheme(
    primary = MedicineGreen,
    onPrimary = Color.White,
    primaryContainer = MedicineGreenSoft,
    onPrimaryContainer = MedicineGreenBorder,
    secondary = WaterBlue,
    onSecondary = Color.White,
    secondaryContainer = WaterCardBg,
    onSecondaryContainer = WaterBlueDeep,
    tertiary = MorningAmber,
    onTertiary = Color.White,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = IvoryBorder,
  )

private val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
  )

@Composable
fun SeniorCareTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = SeniorIvoryColorScheme,
    typography = SeniorTypography,
    shapes = SeniorShapes,
    content = content,
  )
}
