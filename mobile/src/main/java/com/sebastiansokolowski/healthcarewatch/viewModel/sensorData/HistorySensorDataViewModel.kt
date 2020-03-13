package com.sebastiansokolowski.healthcarewatch.viewModel.sensorData

import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity_
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventEntity
import com.sebastiansokolowski.healthcarewatch.db.entity.SensorEventEntity_
import io.objectbox.BoxStore
import io.objectbox.rx.RxQuery
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Sebastian Sokołowski on 10.03.19.
 */
class HistorySensorDataViewModel
@Inject constructor(boxStore: BoxStore) : SensorEventViewModel(boxStore) {

    override fun getHealthCareEventsObservable(): Observable<MutableList<HealthCareEventEntity>> {
        val startDayTimestamp = getStartDayTimestamp(currentDate.time)
        val endDayTimestamp = getEndDayTimestamp(startDayTimestamp)

        val query = healthCareEventEntityBox.query()
                .orderDesc(HealthCareEventEntity_.__ID_PROPERTY)
                .apply {
                    link(HealthCareEventEntity_.sensorEventEntity)
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