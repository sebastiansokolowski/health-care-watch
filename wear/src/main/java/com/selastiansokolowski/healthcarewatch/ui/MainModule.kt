package com.selastiansokolowski.healthcarewatch.ui

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
@Module
class MainModule {

    @Provides
    fun provideMainView(mainActivity: MainActivity): MainView {
        return mainActivity
    }

    @Provides
    fun provideMainPresenter(context: Context, mainView: MainView): MainPresenter {
        return MainPresenter(context, mainView)
    }
}