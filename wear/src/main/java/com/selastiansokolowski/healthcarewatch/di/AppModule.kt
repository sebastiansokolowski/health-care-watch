package com.selastiansokolowski.healthcarewatch.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    fun provideWearableDataClient(app: Application): WearableDataClient {
        return WearableDataClient(app)
    }

    @Provides
    fun provideSensorManager(app: Application): SensorManager {
        return app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @Singleton
    fun provideSensorDataModel(wearableDataClient: WearableDataClient, sensorManager: SensorManager): SensorDataModel {
        return SensorDataModel(wearableDataClient, sensorManager)
    }
}