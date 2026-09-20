package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CutterLogMainScreen
import com.example.ui.theme.CutterLogTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CutterLogTheme {
                val mainViewModel: MainViewModel = viewModel()
                CutterLogMainScreen(viewModel = mainViewModel)
            }
        }
    }
}


