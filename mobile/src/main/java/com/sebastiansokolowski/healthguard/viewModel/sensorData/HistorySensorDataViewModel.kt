package com.sebastiansokolowski.healthguard.viewModel.sensorData

import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity_
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthguard.db.entity.SensorEventEntity_
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistorySensorDataViewModel
@Inject constructor(boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    override fun getHealthEventsObservable(): Observable<MutableList<HealthEventEntity>> {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)
        val endDayTimestamp = getEndDayTimestamp(startDayTimestamp)

        val query = healthEventEntityBox.query()
                .orderDesc(HealthEventEntity_.__ID_PROPERTY)
                .apply {
                    link(HealthEventEntity_.sensorEventEntity)
                            .between(SensorEventEntity_.timestamp, startDayTimestamp, endDayTimestamp)
                            .equal(SensorEventEntity_.type, sensorType.toLong())
                }.build()

        return RxQuery.observable(query)
                .take(1)
    }

    override fun getSensorEventsObservable(): Observable<MutableList<SensorEventEntity>> {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)
        val endDayTimestamp = getEndDayTimestamp(startDayTimestamp)

        val query = sensorEventEntityBox.query().apply {
            equal(SensorEventEntity_.type, sensorType.toLong())
            between(SensorEventEntity_.timestamp, startDayTimestamp, endDayTimestamp)
        }.build()

        return RxQuery.observable(query)
                .take(1)
    }
}