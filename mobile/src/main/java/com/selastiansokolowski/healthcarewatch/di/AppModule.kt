package com.selastiansokolowski.healthcarewatch.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.NotificationModel
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import com.selastiansokolowski.shared.db.entity.MyObjectBox
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
    @Singleton
    fun provideNotificationModel(app: Application, prefs: SharedPreferences): NotificationModel =
            NotificationModel(app, prefs)

    @Provides
    @Singleton
    fun provideSensorDataModel(context: Context, wearableDataClient: WearableDataClient, boxStore: BoxStore, notificationModel: NotificationModel): SensorDataModel =
            SensorDataModel(context, wearableDataClient, boxStore, notificationModel)

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