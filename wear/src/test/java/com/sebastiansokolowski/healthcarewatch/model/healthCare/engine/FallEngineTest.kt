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
import io.mockk.junit5.MockKExtension
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@ExtendWith(MockKExtension::class)
class FallEngineTest {
    private val testObj = FallEngine()
    private val sensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    @MockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var stepDetector: StepDetector

    @BeforeEach
    fun setUp() {
        testObj.stepDetector = stepDetector
        testObj.setupEngine(sensorObservable, notifyObservable, measurementSettings)
        testObj.startEngine()

        every { stepDetector.isStepDetected() } returns true
        every { measurementSettings.fallThreshold } returns 2

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun test() {
        Thread {
            val values = floatArrayOf(-7.8f, 2.8f, -14.8f)
            val values2 = floatArrayOf(1.4f, 0.2f, -5.6f)
            val values3 = floatArrayOf(0f, 0f, 0f)

            val sensorEvent = getMockedSensorEvent(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
            val sensorEvent2 = getMockedSensorEvent(Sensor.TYPE_LINEAR_ACCELERATION, values = values2)
            val sensorEvent3 = getMockedSensorEvent(Sensor.TYPE_LINEAR_ACCELERATION, values = values3)
            for (i in 0..10) {
                sensorObservable.onNext(sensorEvent3)
            }
            sensorObservable.onNext(sensorEvent)
            sensorObservable.onNext(sensorEvent2)
        }.start()

        assertNotNull(notifyObservable.blockingFirst())
    }
}