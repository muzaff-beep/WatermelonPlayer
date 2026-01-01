package com.watermelon.player.di

import com.watermelon.player.ui.screens.HomeViewModel
import com.watermelon.player.ui.screens.PlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel() }
    viewModel { PlayerViewModel() }
}
