package com.selastiansokolowski.healthcarewatch

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Module
abstract class ActivitiesBindingModule {
    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity
}