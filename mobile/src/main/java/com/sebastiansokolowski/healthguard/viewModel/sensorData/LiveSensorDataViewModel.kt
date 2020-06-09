package com.sebastiansokolowski.healthguard.viewModel.sensorData

import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import io.objectbox.BoxStore
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class LiveSensorDataViewModel
@Inject constructor(private val measurementModel: MeasurementModel, private val sensorDataModel: SensorDataModel, boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()

    private var sensorEventsObservable: Observable<MutableList<SensorEventEntity>> = initSensorEvents()
    private var healthEventsObservable: Observable<MutableList<HealthEventEntity>> = initHealthEvents()

    private val currentTimestamp = Date().time

    init {
        initMeasurementState()
    }

    private fun initMeasurementState() {
        val disposable = measurementModel.measurementStateObservable
                .subscribeOn(Schedulers.io())
                .subscribe {
                    if (it) {
                        sensorEventsObservable = initSensorEvents()
                        healthEventsObservable = initHealthEvents()
                    } else {
                        sensorEventsObservable = Observable.just(mutableListOf())
                        healthEventsObservable = Observable.just(mutableListOf())
                    }
                    refreshView()
                }

        disposables.add(disposable)
    }

    private fun initSensorEvents(): Observable<MutableList<SensorEventEntity>> {
        val sensorEventEntities = mutableListOf<SensorEventEntity>()
        return sensorDataModel.sensorsObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == sensorType }
                .filter { it.timestamp >= currentTimestamp }
                .map {
                    sensorEventEntities.add(it)
                    sensorEventEntities
                }
    }

    private fun initHealthEvents(): Observable<MutableList<HealthEventEntity>> {
        val healthEventEntities = mutableListOf<HealthEventEntity>()

        return sensorDataModel.healthEventObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensorEventEntity.target.type == sensorType }
                .filter { it.sensorEventEntity.target.timestamp >= currentTimestamp }
                .map {
                    healthEventEntities.add(it)
                    healthEventEntities.sortByDescending { it.id }
                    healthEventEntities
                }
    }

    override fun getHealthEventsObservable(): Observable<MutableList<HealthEventEntity>> {
        return healthEventsObservable
    }

    override fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>> {
        return sensorEventsObservable
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}