package com.sebastiansokolowski.healthguard.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.model.HealthCareModel
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
    fun provideWearableDataClient(app: Application): WearableDataClient {
        return WearableDataClient(app)
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
    fun provideSensorDataModel(measurementModel: MeasurementModel, wearableDataClient: WearableDataClient, sensorManager: SensorManager, healthCareModel: HealthCareModel): SensorDataModel {
        return SensorDataModel(measurementModel, wearableDataClient, sensorManager, healthCareModel)
    }

    @Provides
    @Singleton
    fun provideHealthCareModel(wearableDataClient: WearableDataClient): HealthCareModel =
            HealthCareModel(wearableDataClient)
}