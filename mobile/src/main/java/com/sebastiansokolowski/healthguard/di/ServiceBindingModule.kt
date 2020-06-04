package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.service.MeasurementService
import com.sebastiansokolowski.healthguard.service.WearableService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 10.07.18.
 */
@Suppress("unused")
@Module
abstract class ServiceBindingModule {
    @ContributesAndroidInjector
    abstract fun messageReceiverService(): WearableService

    @ContributesAndroidInjector
    abstract fun measurementService(): MeasurementService
}