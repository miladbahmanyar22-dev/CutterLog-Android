package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.CutterLogTopHeader
import com.example.ui.components.Modern3DBottomNavigation
import com.example.ui.theme.DarkBg
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CutterLogMainScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Force RTL layout for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CutterLogTopHeader(
                    onSettingsClick = { viewModel.setSelectedTab(5) }
                )
            },
            bottomBar = {
                Modern3DBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.setSelectedTab(it) }
                )
            },
            containerColor = DarkBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBg)
            ) {
                when (selectedTab) {
                    0 -> WorkspaceTab(viewModel = viewModel)
                    1 -> ArchiveTab(viewModel = viewModel)
                    2 -> FinanceTab(viewModel = viewModel)
                    3 -> TimerTab(viewModel = viewModel)
                    4 -> CutterPilotTab(viewModel = viewModel)
                    5 -> SettingsTab(viewModel = viewModel)
                }
            }
        }
    }
}
