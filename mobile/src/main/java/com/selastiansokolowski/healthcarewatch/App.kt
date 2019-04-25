package com.selastiansokolowski.healthcarewatch

import com.selastiansokolowski.healthcarewatch.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
class App : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .create(this)
                .build()
    }
}