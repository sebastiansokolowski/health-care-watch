package com.sebastiansokolowski.healthcarewatch.di

import com.sebastiansokolowski.healthcarewatch.receiver.BatteryLowLevelReceiver
import com.sebastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.sebastiansokolowski.healthcarewatch.service.SensorService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 10.07.18.
 */
@Suppress("unused")
@Module
abstract class ServiceBindingModule {
    @ContributesAndroidInjector
    abstract fun sensorService(): SensorService

    @ContributesAndroidInjector
    abstract fun batteryLowLevelReceiver(): BatteryLowLevelReceiver

    @ContributesAndroidInjector
    abstract fun messageReceiverService(): MessageReceiverService
}