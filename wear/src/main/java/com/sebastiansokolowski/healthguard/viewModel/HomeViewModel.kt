package com.sebastiansokolowski.healthguard.viewModel

import android.Manifest
import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import com.sebastiansokolowski.healthguard.BuildConfig
import com.sebastiansokolowski.healthguard.client.WearableClient
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.shared.dataModel.SensorEvent
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
@Inject constructor(private val sensorDataModel: SensorDataModel, private val wearableClient: WearableClient) : ViewModel() {

    private val MY_PERMISSIONS_REQUEST_BODY_SENSORS = 12

    private val disposables = CompositeDisposable()
    private var heartRateDisposable: Disposable? = null

    val measurementState: LiveData<Boolean> by lazy {
        initMeasurementStateLiveData()
    }

    val heartRate: MutableLiveData<String> = MutableLiveData()

    var chartDataCounter = 0
    val chartData: MutableLiveData<MutableList<Entry>> = MutableLiveData()

    init {
        initHeartRate()
    }

    private fun initHeartRate() {
        sensorDataModel.measurementStateObservable
                .subscribeOn(Schedulers.io())
                .filter { it }
                .subscribe {
                    heartRateDisposable?.dispose()
                    sensorDataModel.heartRateObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete {
                                heartRate.postValue("")
                                chartData.postValue(mutableListOf())
                            }
                            .subscribe { sensorEvent ->
                                setChartData(sensorEvent)
                                setHeartRate(sensorEvent)
                            }.let {
                                heartRateDisposable = it
                            }
                }.let {
                    disposables.add(it)
                }
    }

    private fun setChartData(sensorEvent: SensorEvent) {
        val data = chartData.value ?: mutableListOf()

        if (data.size >= 7) {
            data.removeAt(0)
        }

        chartDataCounter++
        val entry = Entry(chartDataCounter.toFloat(), sensorEvent.values[0].roundToInt().toFloat())
        data.add(entry)

        chartData.value = data
    }

    private fun setHeartRate(sensorEvent: SensorEvent) {
        var result = ""
        if (sensorEvent.values.isNotEmpty()) {
            result = sensorEvent.values[0].toInt().toString()
        }
        heartRate.postValue(result)
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
                                wearableClient.requestStartMeasurement()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        heartRateDisposable?.dispose()
        disposables.clear()
    }
}