package com.sebastiansokolowski.healthguard.dataModel

import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject

data class SensorsObservable(
        var heartRateObservable: ReplaySubject<SensorEvent> = ReplaySubject.createWithSize(10),
        val stepDetectorObservable: PublishSubject<SensorEvent> = PublishSubject.create(),
        val linearAccelerationObservable: PublishSubject<SensorEvent> = PublishSubject.create()
)