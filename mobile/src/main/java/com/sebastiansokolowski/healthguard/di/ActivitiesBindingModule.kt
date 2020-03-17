package com.sebastiansokolowski.healthguard.di

import com.sebastiansokolowski.healthguard.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Suppress("unused")
@Module
abstract class ActivitiesBindingModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun mainActivity(): MainActivity
}