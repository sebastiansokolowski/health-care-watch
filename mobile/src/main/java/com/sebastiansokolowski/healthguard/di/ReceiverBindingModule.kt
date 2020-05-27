package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.receiver.BootCompletedReceiver
import com.sebastiansokolowski.healthguard.receiver.CancelSMSNotificationReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 10.07.18.
 */
@Suppress("unused")
@Module
abstract class ReceiverBindingModule {
    @ContributesAndroidInjector
    abstract fun bootCompletedReceiver(): BootCompletedReceiver

    @ContributesAndroidInjector
    abstract fun cancelSMSNotificationReceiver(): CancelSMSNotificationReceiver
}