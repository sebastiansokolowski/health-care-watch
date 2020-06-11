package com.sebastiansokolowski.healthguard.viewModel.sensorData

import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity_
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity_
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 23.06.19.
 */
class LiveSensorDataViewModel
@Inject constructor(boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    private val currentTimestamp = Date().time

    override fun getHealthEventsObservable(): Observable<MutableList<HealthEventEntity>> {
        val query = healthEventEntityBox.query()
                .apply {
                    link(HealthEventEntity_.sensorEventEntity)
                            .greater(SensorEventEntity_.timestamp, currentTimestamp)
                            .equal(SensorEventEntity_.type, sensorType.toLong())
                }.orderDesc(HealthEventEntity_.__ID_PROPERTY)
                .build()

        return RxQuery.observable(query)
    }

    override fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>> {
        val query = sensorEventEntityBox.query().apply {
            equal(SensorEventEntity_.type, sensorType.toLong())
            greater(SensorEventEntity_.timestamp, currentTimestamp)
        }.build()

        return RxQuery.observable(query)
    }
}