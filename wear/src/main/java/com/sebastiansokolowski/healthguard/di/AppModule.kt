package com.sebastiansokolowski.healthguard.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.HealthGuardModel
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import com.sebastiansokolowski.healthguard.model.SensorDataModel
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
    fun provideWearableClient(app: Application): WearableClient {
        return WearableClient(app)
    }

    @Provides
    fun provideSensorManager(app: Application): SensorManager {
        return app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    fun provideMeasurementModel(context: Context): MeasurementModel {
        return MeasurementModel(context)
    }

    @Provides
    @Singleton
    fun provideSensorDataModel(measurementModel: MeasurementModel, wearableClient: WearableClient, sensorManager: SensorManager, healthGuardModel: HealthGuardModel): SensorDataModel {
        return SensorDataModel(measurementModel, wearableClient, sensorManager, healthGuardModel)
    }

    @Provides
    @Singleton
    fun provideHealthGuardModel(wearableClient: WearableClient): HealthGuardModel =
            HealthGuardModel(wearableClient)
}