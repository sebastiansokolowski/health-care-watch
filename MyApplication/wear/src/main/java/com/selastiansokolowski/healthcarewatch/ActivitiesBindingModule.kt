package com.selastiansokolowski.healthcarewatch

import com.selastiansokolowski.healthcarewatch.ui.MainActivity
import com.selastiansokolowski.healthcarewatch.ui.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
@Module
abstract class ActivitiesBindingModule {

    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun mainActivity(): MainActivity
}