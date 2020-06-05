package com.sebastiansokolowski.healthguard.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.db.entity.MyObjectBox
import com.sebastiansokolowski.healthguard.model.*
import dagger.Module
import dagger.Provides
import io.objectbox.BoxStore
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
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideWearableClient(context: Context): WearableClient =
            WearableClient(context)

    @Provides
    @Singleton
    fun provideBoxStore(context: Context): BoxStore = MyObjectBox.builder()
            .androidContext(context)
            .build()

    @Provides
    fun provideSettingsModel(app: Application): SettingsModel {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        return SettingsModel(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideLocationModel(context: Context): LocationModel =
            LocationModel(context)

    @Provides
    @Singleton
    fun provideNotificationModel(context: Context, settingsModel: SettingsModel, locationModel: LocationModel): NotificationModel =
            NotificationModel(context, settingsModel, locationModel)

    @Provides
    @Singleton
    fun provideSensorDataModel(context: Context, notificationModel: NotificationModel, boxStore: BoxStore): SensorDataModel =
            SensorDataModel(context, notificationModel, boxStore)

    @Provides
    @Singleton
    fun provideMeasurementModel(context: Context, wearableClient: WearableClient, boxStore: BoxStore, settingsModel: SettingsModel, sensorDataModel: SensorDataModel): MeasurementModel =
            MeasurementModel(context, wearableClient, boxStore, settingsModel, sensorDataModel)

    @Provides
    @Singleton
    fun provideSetupModel(wearableClient: WearableClient, measurementModel: MeasurementModel, settingsModel: SettingsModel): SetupModel =
            SetupModel(wearableClient, measurementModel, settingsModel)

    @Provides
    @Singleton
    fun provideShareDataModel(context: Context, boxStore: BoxStore): ShareDataModel =
            ShareDataModel(context, boxStore)

}