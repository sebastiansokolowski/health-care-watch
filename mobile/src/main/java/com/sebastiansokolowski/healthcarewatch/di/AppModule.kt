package com.sebastiansokolowski.healthcarewatch.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.sebastiansokolowski.healthcarewatch.client.WearableDataClient
import com.sebastiansokolowski.healthcarewatch.db.entity.MyObjectBox
import com.sebastiansokolowski.healthcarewatch.model.*
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
    fun provideSetupModel(wearableDataClient: WearableDataClient, sensorDataModel: SensorDataModel, settingsModel: SettingsModel): SetupModel =
            SetupModel(wearableDataClient, sensorDataModel, settingsModel)

    @Provides
    @Singleton
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideNotificationModel(app: Application, prefs: SharedPreferences): NotificationModel =
            NotificationModel(app, prefs)

    @Provides
    @Singleton
    fun provideSensorDataModel(context: Context, wearableDataClient: WearableDataClient, notificationModel: NotificationModel, boxStore: BoxStore, settingsModel: SettingsModel): SensorDataModel =
            SensorDataModel(context, wearableDataClient, notificationModel, boxStore, settingsModel)

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
    fun provideWearableDataClient(context: Context): WearableDataClient =
            WearableDataClient(context)

}