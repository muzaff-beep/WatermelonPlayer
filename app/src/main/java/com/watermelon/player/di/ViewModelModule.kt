package com.watermelon.player.di

import com.watermelon.player.ui.screens.HomeViewModel
import com.watermelon.player.ui.screens.PlayerViewModel
import com.watermelon.player.ui.screens.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { SettingsViewModel(get()) }

    // Add other ViewModels as needed, e.g., for billing, update, etc.
}
