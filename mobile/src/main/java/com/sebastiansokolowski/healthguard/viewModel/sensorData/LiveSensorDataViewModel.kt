package com.sebastiansokolowski.healthguard.viewModel.sensorData

import com.sebastiansokolowski.healthguard.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import io.objectbox.BoxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class LiveSensorDataViewModel
@Inject constructor(private val sensorDataModel: SensorDataModel, boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    private val disposables = CompositeDisposable()

    private val sensorEventsObservable: BehaviorSubject<MutableList<SensorEventEntity>> = BehaviorSubject.createDefault(mutableListOf())
    private val healthCareEventsObservable: BehaviorSubject<MutableList<HealthCareEventEntity>> = BehaviorSubject.createDefault(mutableListOf())

    init {
        initSensorEvents()
        initHealthCareEvents()
    }

    private fun initSensorEvents() {
        val sensorEventEntities = mutableListOf<SensorEventEntity>()

        val disposable = sensorDataModel.sensorsObservable
                .subscribeOn(Schedulers.io())
                .filter { it.type == sensorType }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    sensorEventEntities.add(it)
                    sensorEventsObservable.onNext(sensorEventEntities)
                }

        disposables.add(disposable)
    }

    private fun initHealthCareEvents() {
        val healthCareEventEntities = mutableListOf<HealthCareEventEntity>()

        val disposable = sensorDataModel.healthCareEventObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensorEventEntity.target?.type == sensorType }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthCareEventEntities.add(it)
                    healthCareEventsObservable.onNext(healthCareEventEntities)
                }

        disposables.add(disposable)
    }

    override fun getHealthCareEventsObservable(): Observable<MutableList<HealthCareEventEntity>> {
        return healthCareEventsObservable
    }

    override fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>> {
        return sensorEventsObservable
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}