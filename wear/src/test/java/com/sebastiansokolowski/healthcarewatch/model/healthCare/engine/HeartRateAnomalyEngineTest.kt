package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.hardware.SensorEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.mockito.Mockito

/**
 * Created by Sebastian Sokołowski on 17.09.19.
 */
class HeartRateAnomalyEngineTest {

    private val testObj = HeartRateAnomalyEngine()
    private val sensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    @Before
    fun setUp() {
        testObj.setupEngine(sensorObservable, notifyObservable)
    }


    private fun getMockedSensorEvent(): SensorEvent {
        val sensorEvent = Mockito.mock(SensorEvent::class.java)

        val sensor = Mockito.mock(Sensor::class.java)
        Mockito.`when`(sensor.type).thenReturn(Sensor.TYPE_STEP_DETECTOR)

        val sensorField = sensorEvent.javaClass.getField("sensor")
        sensorField.isAccessible = true
        sensorField.set(sensorEvent, sensor)

        return sensorEvent
    }
}