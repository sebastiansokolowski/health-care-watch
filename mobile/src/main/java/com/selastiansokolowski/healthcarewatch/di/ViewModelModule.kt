/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.selastiansokolowski.healthcarewatch.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.selastiansokolowski.healthcarewatch.viewModel.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryDataViewModel::class)
    abstract fun bindHistoryDataViewModel(historyDataViewModel: HistoryDataViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LiveSensorDataViewModel::class)
    abstract fun bindLiveSensorDataViewModel(liveSensorDataViewModel: LiveSensorDataViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistorySensorDataViewModel::class)
    abstract fun bindHistorySensorDataViewModel(historySensorDataViewModel: HistorySensorDataViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
