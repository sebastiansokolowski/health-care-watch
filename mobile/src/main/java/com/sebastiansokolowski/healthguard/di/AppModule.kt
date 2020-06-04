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
    fun provideSharedPreference(app: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    fun provideSettingsModel(app: Application): SettingsModel {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        return SettingsModel(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSetupModel(wearableClient: WearableClient, sensorDataModel: SensorDataModel, settingsModel: SettingsModel): SetupModel =
            SetupModel(wearableClient, sensorDataModel, settingsModel)

    @Provides
    @Singleton
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

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
    fun provideSensorDataModel(context: Context, wearableClient: WearableClient, notificationModel: NotificationModel, boxStore: BoxStore, settingsModel: SettingsModel): SensorDataModel =
            SensorDataModel(context, wearableClient, notificationModel, boxStore, settingsModel)

    @Provides
    @Singleton
    fun provideShareDataModel(context: Context, boxStore: BoxStore): ShareDataModel =
            ShareDataModel(context, boxStore)

    @Provides
    @Singleton
    fun provideBoxStore(context: Context): BoxStore = MyObjectBox.builder()
            .androidContext(context)
            .build()

    @Provides
    @Singleton
    fun provideWearableClient(context: Context): WearableClient =
            WearableClient(context)

}