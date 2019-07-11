package com.selastiansokolowski.healthcarewatch.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.preference.PreferenceManager
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.HealthCareModel
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.healthcarewatch.model.SettingsModel
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
    fun provideSharedPreference(app: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(app)

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
    fun provideSettingsModel(context: Context, sharedPreferences: SharedPreferences): SettingsModel {
        return SettingsModel(context, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSensorDataModel(settingsModel: SettingsModel, wearableDataClient: WearableDataClient, sensorManager: SensorManager): SensorDataModel {
        return SensorDataModel(settingsModel, wearableDataClient, sensorManager)
    }

    @Provides
    @Singleton
    fun provideHealthCareModel(sensorDataModel: SensorDataModel, wearableDataClient: WearableDataClient, sharedPreferences: SharedPreferences): HealthCareModel =
            HealthCareModel(sensorDataModel, wearableDataClient, sharedPreferences)
}