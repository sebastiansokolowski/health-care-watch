package com.selastiansokolowski.healthcarewatch.di

import com.selastiansokolowski.healthcarewatch.ui.HomeFragment
import com.selastiansokolowski.healthcarewatch.ui.WatchDataFragment
import com.selastiansokolowski.healthcarewatch.ui.SettingsFragment
import com.selastiansokolowski.healthcarewatch.ui.sensorData.SensorDataFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeSensorsDataFragment(): WatchDataFragment

    @ContributesAndroidInjector
    abstract fun contributeSensorDataFragment(): SensorDataFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}
