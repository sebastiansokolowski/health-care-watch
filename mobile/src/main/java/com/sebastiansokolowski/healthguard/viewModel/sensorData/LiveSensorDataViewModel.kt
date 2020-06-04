package com.sebastiansokolowski.healthguard.viewModel.sensorData

import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
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
    private val healthEventsObservable: BehaviorSubject<MutableList<HealthEventEntity>> = BehaviorSubject.createDefault(mutableListOf())

    init {
        initSensorEvents()
        initHealthEvents()
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

    private fun initHealthEvents() {
        val healthEventEntities = mutableListOf<HealthEventEntity>()

        val disposable = sensorDataModel.healthEventObservable
                .subscribeOn(Schedulers.io())
                .filter { it.sensorEventEntity.target?.type == sensorType }
                .map {
                    healthEventEntities.add(it)
                    healthEventEntities.sortByDescending { it.id }
                    healthEventEntities
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    healthEventsObservable.onNext(healthEventEntities)
                }

        disposables.add(disposable)
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