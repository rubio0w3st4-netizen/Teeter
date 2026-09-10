package com.varnok.eslin.teeter.di

import com.varnok.eslin.teeter.audio.Haptics
import com.varnok.eslin.teeter.audio.SoundManager
import com.varnok.eslin.teeter.data.local.TeeterPrefs
import com.varnok.eslin.teeter.data.repository.LevelRepositoryImpl
import com.varnok.eslin.teeter.data.repository.ProgressRepositoryImpl
import com.varnok.eslin.teeter.data.repository.SettingsRepositoryImpl
import com.varnok.eslin.teeter.domain.repository.LevelRepository
import com.varnok.eslin.teeter.domain.repository.ProgressRepository
import com.varnok.eslin.teeter.domain.repository.SettingsRepository
import com.varnok.eslin.teeter.domain.usecase.AwardBoardUseCase
import com.varnok.eslin.teeter.domain.usecase.BuildBoardUseCase
import com.varnok.eslin.teeter.domain.usecase.FinishRunUseCase
import com.varnok.eslin.teeter.domain.usecase.NextLevelUseCase
import com.varnok.eslin.teeter.domain.usecase.StartRunUseCase
import com.varnok.eslin.teeter.domain.usecase.StatsUseCase
import com.varnok.eslin.teeter.presentation.TeeterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TeeterPrefs(androidContext()) }
    single<LevelRepository> { LevelRepositoryImpl() }
    single<ProgressRepository> { ProgressRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single { SoundManager(androidContext(), get()) }
    single { Haptics(androidContext(), get()) }
    factory { StartRunUseCase(get()) }
    factory { FinishRunUseCase(get(), get()) }
    factory { BuildBoardUseCase(get(), get()) }
    factory { NextLevelUseCase(get(), get()) }
    factory { AwardBoardUseCase(get(), get()) }
    factory { StatsUseCase(get()) }
    viewModel { TeeterViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
