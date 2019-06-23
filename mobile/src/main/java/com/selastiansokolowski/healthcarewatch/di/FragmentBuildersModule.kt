package com.selastiansokolowski.healthcarewatch.di

import com.selastiansokolowski.healthcarewatch.ui.HistoryDataFragment
import com.selastiansokolowski.healthcarewatch.ui.HomeFragment
import com.selastiansokolowski.healthcarewatch.ui.LiveDataFragment
import com.selastiansokolowski.healthcarewatch.ui.SettingsFragment
import com.selastiansokolowski.healthcarewatch.ui.sensorData.HistorySensorDataFragment
import com.selastiansokolowski.healthcarewatch.ui.sensorData.LiveSensorDataFragment
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
}
