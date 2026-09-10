package com.varnok.eslin.teeter.presentation

sealed interface Route {
    data object Tutorial : Route
    data object Menu : Route
    data object Levels : Route
    data object Play : Route
    data object Result : Route
    data object Awards : Route
    data object Stats : Route
    data object Settings : Route
}
