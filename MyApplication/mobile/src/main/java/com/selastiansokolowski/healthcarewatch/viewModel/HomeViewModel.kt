package com.selastiansokolowski.healthcarewatch.viewModel

import android.arch.lifecycle.ViewModel
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HomeViewModel
@Inject constructor(sensorDataModel: SensorDataModel) : ViewModel() {
    private val disposables = CompositeDisposable()


}