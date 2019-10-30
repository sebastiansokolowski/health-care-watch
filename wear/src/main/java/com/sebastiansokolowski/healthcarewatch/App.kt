package com.sebastiansokolowski.healthcarewatch

import com.sebastiansokolowski.healthcarewatch.di.DaggerAppComponent
import com.sebastiansokolowski.healthcarewatch.model.HealthCareModel
import com.sebastiansokolowski.healthcarewatch.utils.LogUtils
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
class App : DaggerApplication() {

    @Inject
    lateinit var healthCareModel: HealthCareModel

    override fun onCreate() {
        super.onCreate()

        LogUtils.setupLogcat(applicationInfo)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent
                .builder()
                .create(this)
                .build()
    }
}