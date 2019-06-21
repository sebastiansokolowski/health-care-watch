package com.selastiansokolowski.healthcarewatch.di

import com.selastiansokolowski.healthcarewatch.ui.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Suppress("unused")
@Module
abstract class ActivitiesBindingModule {
    @ContributesAndroidInjector
    abstract fun homeActivity(): HomeActivity
}