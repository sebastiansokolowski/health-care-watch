package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.ui.*
import com.sebastiansokolowski.healthguard.ui.sensorData.HistorySensorDataFragment
import com.sebastiansokolowski.healthguard.ui.sensorData.LiveSensorDataFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeWatchDataFragment(): LiveDataFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryDataFragment(): HistoryDataFragment

    @ContributesAndroidInjector
    abstract fun contributeLiveSensorDataFragment(): LiveSensorDataFragment

    @ContributesAndroidInjector
    abstract fun contributeSensorDataFragment(): HistorySensorDataFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeAdvancedSettingsFragment(): AdvancedSettingsFragment
}
