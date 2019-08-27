package com.selastiansokolowski.healthcarewatch.viewModel

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.selastiansokolowski.healthcarewatch.client.WearableDataClient
import com.selastiansokolowski.healthcarewatch.model.SensorDataModel
import io.reactivex.BackpressureStrategy
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class HomeViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val wearableDataClient: WearableDataClient) : ViewModel() {

    private val MY_PERMISSIONS_REQUEST_BODY_SENSORS = 12

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }

    val heartRate: LiveData<String> by lazy {
        initHeartRateLiveData()
    }

    private fun initHeartRateLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObservable.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) {
            var result = ""
            if (it != null && sensorDataModel.measurementRunning) {
                result = it.toString()
            }
            return@map result
        }
    }

    private fun initMeasurementStateLiveData(): LiveData<Boolean> {
        val measurementStateFlowable = sensorDataModel.measurementStateObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(measurementStateFlowable)
    }

    fun toggleMeasurementState() {
        sensorDataModel.toggleMeasurementState()
    }

    fun requestPermissions(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.BODY_SENSORS),
                    MY_PERMISSIONS_REQUEST_BODY_SENSORS)
            sensorDataModel.stopMeasurement()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BODY_SENSORS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    wearableDataClient.requestStartMeasurement()
                }
            }
        }
    }
}