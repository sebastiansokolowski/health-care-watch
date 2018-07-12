package com.selastiansokolowski.healthcarewatch

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

/**
 * Created by Sebastian Sokołowski on 08.07.18.
 */
@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            ActivitiesBindingModule::class,
            ServiceBindingModule::class
        ]
)
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun create(app: Application): Builder

        fun build(): AppComponent
    }
}