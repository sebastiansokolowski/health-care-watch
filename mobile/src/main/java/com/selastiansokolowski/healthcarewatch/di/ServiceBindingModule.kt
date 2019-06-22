package com.selastiansokolowski.healthcarewatch.di

import com.selastiansokolowski.healthcarewatch.service.MeasurementService
import com.selastiansokolowski.healthcarewatch.service.MessageReceiverService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 10.07.18.
 */
@Suppress("unused")
@Module
abstract class ServiceBindingModule {
    @ContributesAndroidInjector
    abstract fun messageReceiverService(): MessageReceiverService

    @ContributesAndroidInjector
    abstract fun measurementService(): MeasurementService
}