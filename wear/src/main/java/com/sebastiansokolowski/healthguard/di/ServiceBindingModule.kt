package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.service.MessageReceiverService
import com.sebastiansokolowski.healthguard.service.SensorService
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
    abstract fun messageReceiverService(): MessageReceiverService
}