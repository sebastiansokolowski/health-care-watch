package com.sebastiansokolowski.healthguard.viewModel

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.*
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.client.WearableDataClient
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
class HomeViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, private val wearableDataClient: WearableDataClient) : ViewModel() {

    private val MY_PERMISSIONS_REQUEST_BODY_SENSORS = 12

    private val disposables = CompositeDisposable()


    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }

    val heartRate: LiveData<String> by lazy {
        initHeartRateLiveData()
    }

    var chartDataCounter = 0
    val chartData: MutableLiveData<MutableList<Entry>> = MutableLiveData()

    init {
        initChartData()
    }

    private fun initHeartRateLiveData(): LiveData<String> {
        val sensorDataModelFlowable = sensorDataModel.heartRateObservable.toFlowable(BackpressureStrategy.LATEST)
        val sensorDataModelLiveData = LiveDataReactiveStreams.fromPublisher(sensorDataModelFlowable)
        return Transformations.map(sensorDataModelLiveData) {
            var result = ""
            if (it != null && sensorDataModel.measurementRunning) {
                result = it.values[0].roundToInt().toString()
            }
            return@map result
        }
    }

    private fun initChartData() {
        val disposable: Disposable?
        disposable = sensorDataModel.heartRateObservable
                .subscribeOn(Schedulers.io())
                .map {
                    val data = chartData.value ?: mutableListOf()

                    if (data.size >= 7) {
                        data.removeAt(0)
                    }

                    chartDataCounter++
                    val entry = Entry(chartDataCounter.toFloat(), it.values[0].roundToInt().toFloat())
                    data.add(entry)

                    return@map data
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    chartData.value = it
                }
        disposables.add(disposable)
    }

    private fun initMeasurementStateLiveData(): LiveData<Boolean> {
        val measurementStateFlowable = sensorDataModel.measurementStateObservable.toFlowable(BackpressureStrategy.LATEST)
        return LiveDataReactiveStreams.fromPublisher(measurementStateFlowable)
    }

    fun toggleMeasurementState() {
        sensorDataModel.toggleMeasurementState()
    }

    fun requestPermissions(activity: Activity) {
        val missingPermissions = getMissingPermissions(activity)
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    missingPermissions,
                    MY_PERMISSIONS_REQUEST_BODY_SENSORS)
            sensorDataModel.stopMeasurement()
        }
    }

    private fun getMissingPermissions(activity: Activity): Array<String> {
        val missingPermissions = mutableListOf<String>()

        val bodySensorPermissionGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
        val writePermissionGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!bodySensorPermissionGranted) {
            missingPermissions.add(Manifest.permission.BODY_SENSORS)
        }
        if (BuildConfig.DEBUG && !writePermissionGranted) {
            missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return missingPermissions.toTypedArray()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BODY_SENSORS -> {
                permissions.forEachIndexed { index: Int, s: String ->
                    if (s == Manifest.permission.BODY_SENSORS) {
                        val grantResult = grantResults.getOrNull(index)
                        grantResult?.let {
                            if (it == PackageManager.PERMISSION_GRANTED) {
                                wearableDataClient.requestStartMeasurement()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}