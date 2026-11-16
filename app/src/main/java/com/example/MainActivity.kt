package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.DailyCheckRepository
import com.example.data.dailyCheckDataStore
import com.example.ui.DailyCheckScreen
import com.example.ui.DailyCheckViewModel
import com.example.ui.theme.SeniorCareTheme

class MainActivity : ComponentActivity() {

  private val repository by lazy { DailyCheckRepository(applicationContext.dailyCheckDataStore) }

  private val viewModel: DailyCheckViewModel by viewModels {
    DailyCheckViewModel.factory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    setContent {
      SeniorCareTheme {
        DailyCheckScreen(viewModel = viewModel)
      }
    }
  }
}
