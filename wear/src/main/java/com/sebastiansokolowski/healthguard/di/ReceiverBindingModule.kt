package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.receiver.BatteryLowLevelReceiver
import com.sebastiansokolowski.healthguard.receiver.BootCompletedReceiver
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
    abstract fun batteryLowLevelReceiver(): BatteryLowLevelReceiver
}