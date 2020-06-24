package com.sebastiansokolowski.healthguard

import com.sebastiansokolowski.healthguard.di.DaggerAppComponent
import com.sebastiansokolowski.healthguard.model.HealthGuardModel
import com.sebastiansokolowski.healthguard.utils.LogUtils
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
class App : DaggerApplication() {

    @Inject
    lateinit var healthGuardModel: HealthGuardModel

    override fun onCreate() {
        super.onCreate()

        LogUtils.setupLogcat(applicationInfo)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent
                .builder()
                .create(this)
                .build()
    }
}