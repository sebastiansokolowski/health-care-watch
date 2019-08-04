package com.selastiansokolowski.healthcarewatch

import com.selastiansokolowski.healthcarewatch.di.DaggerAppComponent
import com.selastiansokolowski.healthcarewatch.model.HealthCareModel
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
class App : DaggerApplication() {

    @Inject
    lateinit var healthCareModel: HealthCareModel

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent
                .builder()
                .create(this)
                .build()
    }
}