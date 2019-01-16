package com.selastiansokolowski.healthcarewatch

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Module
class AppModule {
    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    fun provideWearableDataClient(app: Application): WearableDataClient{
        return WearableDataClient(app)
    }

    @Provides
    fun provideSensorManager(app: Application): SensorManager {
        return app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}