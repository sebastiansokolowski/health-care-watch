package com.sebastiansokolowski.healthguard

import com.sebastiansokolowski.healthguard.di.DaggerAppComponent
import com.sebastiansokolowski.healthguard.util.LogUtils
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()

        LogUtils.setupLogcat(applicationInfo)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .create(this)
                .build()
    }
}