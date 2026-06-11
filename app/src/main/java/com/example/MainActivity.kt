package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.RecruitmentApp
import com.example.ui.RecruitmentViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RecruitmentViewModel = viewModel()
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    RecruitmentApp(viewModel = viewModel)
                }
            }
        }
    }
}
