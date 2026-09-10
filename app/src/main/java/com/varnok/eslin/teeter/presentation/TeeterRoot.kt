package com.varnok.eslin.teeter.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varnok.eslin.teeter.presentation.screens.AwardsScreen
import com.varnok.eslin.teeter.presentation.screens.LevelsScreen
import com.varnok.eslin.teeter.presentation.screens.MenuScreen
import com.varnok.eslin.teeter.presentation.screens.PlayScreen
import com.varnok.eslin.teeter.presentation.screens.ResultScreen
import com.varnok.eslin.teeter.presentation.screens.SettingsScreen
import com.varnok.eslin.teeter.presentation.screens.StatsScreen
import com.varnok.eslin.teeter.presentation.screens.TutorialScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeeterRoot() {
    val vm: TeeterViewModel = koinViewModel()
    LaunchedEffect(Unit) { vm.boot() }
    when (vm.route) {
        Route.Tutorial -> TutorialScreen(vm)
        Route.Menu -> MenuScreen(vm)
        Route.Levels -> LevelsScreen(vm)
        Route.Play -> PlayScreen(vm)
        Route.Result -> ResultScreen(vm)
        Route.Awards -> AwardsScreen(vm)
        Route.Stats -> StatsScreen(vm)
        Route.Settings -> SettingsScreen(vm)
    }
}
