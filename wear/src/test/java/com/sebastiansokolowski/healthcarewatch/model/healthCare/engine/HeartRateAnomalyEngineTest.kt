package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import android.hardware.SensorEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.MeasurementSettings
import com.sebastiansokolowski.healthcarewatch.model.healthCare.SensorEventMock.Companion.getMockedSensorEvent
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.Semaphore


/**
 * Created by Sebastian Sokołowski on 17.09.19.
 */
@ExtendWith(MockKExtension::class)
class HeartRateAnomalyEngineTest {

    private val sensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    @SpyK
    var testObj: HeartRateAnomalyEngine = HeartRateAnomalyEngine()

    @RelaxedMockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var stepDetector: StepDetector

    @BeforeEach
    fun setUp() {
        testObj.stepDetector = stepDetector
        testObj.setupEngine(sensorObservable, notifyObservable, measurementSettings)
        testObj.startEngine()

        every { stepDetector.isStepDetected() } returns true

        testObj.setupEngine(sensorObservable, notifyObservable, measurementSettings)
    }

    @Test
    fun testTooLowHearthRate_shouldNotify() {
        val sensorEvent = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(30f))
        Thread {
            sensorObservable.onNext(sensorEvent)
        }.start()

        verify(exactly = 1) { testObj.notifyHealthCareEvent(sensorEvent) }
    }

    @Test
    fun testTooLowHearthRate_shouldNotNotify() {
        val sensorEvent = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(41f))
        val sensorEvent2 = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(141f))
        val sensorEvent3 = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(120f))
        val sensorEvent4 = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(110f))
        val sensorEvent5 = getMockedSensorEvent(Sensor.TYPE_HEART_RATE, values = floatArrayOf(90f))
        val semaphore = Semaphore(0)
        Thread {
            every { stepDetector.isStepDetected() } returns true
            sensorObservable.onNext(sensorEvent)
            sensorObservable.onNext(sensorEvent2)
            sensorObservable.onNext(sensorEvent3)
            every { stepDetector.isStepDetected() } returns false
            sensorObservable.onNext(sensorEvent4)
            sensorObservable.onNext(sensorEvent5)

            semaphore.release()
        }.start()
        semaphore.acquire()
        verify(exactly = 0) { testObj.notifyHealthCareEvent(any()) }
    }
}