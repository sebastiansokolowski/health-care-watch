package com.sebastiansokolowski.healthguard.di

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.os.PowerManager
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.AndroidNotificationModel
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
    fun providePowerManager(app: Application): PowerManager {
        return app.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Provides
    @Singleton
    fun provideAndroidNotificationModel(context: Context): AndroidNotificationModel =
            AndroidNotificationModel(context)

    @Provides
    @Singleton
    fun provideSensorDataModel(sensorManager: SensorManager, wearableClient: WearableClient): SensorDataModel {
        return SensorDataModel(sensorManager, wearableClient)
    }

    @Provides
    @Singleton
    fun provideHealthGuardModel(sensorDataModel: SensorDataModel, wearableClient: WearableClient): HealthGuardModel =
            HealthGuardModel(sensorDataModel, wearableClient)

    @Provides
    @Singleton
    fun provideMeasurementModel(context: Context, sensorDataModel: SensorDataModel, healthGuardModel: HealthGuardModel, sensorManager: SensorManager, powerManager: PowerManager, wearableClient: WearableClient): MeasurementModel {
        return MeasurementModel(context, sensorDataModel, healthGuardModel, sensorManager, powerManager, wearableClient)
    }

}