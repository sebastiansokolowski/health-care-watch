package com.selastiansokolowski.healthcarewatch

import com.selastiansokolowski.healthcarewatch.service.SensorService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 10.07.18.
 */
@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract fun sensorService(): SensorService
}