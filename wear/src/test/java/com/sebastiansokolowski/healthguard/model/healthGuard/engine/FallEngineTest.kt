package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@ExtendWith(MockKExtension::class)
class FallEngineTest {

    private val linearAccelerationSensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @SpyK
    var testObj = FallEngine()

    @MockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var stepDetector: StepDetector

    @MockK
    lateinit var sensorsObservable: SensorsObservable

    @BeforeEach
    fun setUp() {
        every { sensorsObservable.linearAccelerationObservable } returns linearAccelerationSensorObservable

        every { stepDetector.isStepDetected() } returns true
        every { measurementSettings.fallSettings.threshold } returns 2
        every { measurementSettings.fallSettings.stepDetector } returns true
        every { measurementSettings.fallSettings.stepDetectorTimeoutS } returns 10
        every { measurementSettings.fallSettings.inactivityDetector } returns false
        every { measurementSettings.fallSettings.sampleCount } returns 10
        every { measurementSettings.measurementId } returns 1

        testObj.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        testObj.stepDetector = stepDetector
        testObj.startEngine()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testFallDetect_shouldNotify() {
        triggerFall()

        verify(exactly = 1) { testObj.notifyHealthEvent(any(), any(), any(), any()) }
    }

    @Test
    fun testFallDetect_WhenStepIsNotDetected_shouldNotNotify() {
        every { stepDetector.isStepDetected() } returns false
        triggerFall()

        verify(exactly = 0) { testObj.notifyHealthEvent(any(), any(), any()) }
    }

    @Test
    fun testFallDetect_WhenStepDetectIsDisabled_shouldNotify() {
        every { stepDetector.isStepDetected() } returns false
        every { measurementSettings.fallSettings.stepDetector } returns false

        triggerFall()

        verify(exactly = 1) { testObj.notifyHealthEvent(any(), any(), any(), any()) }
    }

    @Test
    fun testFallDetect_WhenPostFallDetectionIsEnabled_shouldNotNotify() {
        every { measurementSettings.fallSettings.inactivityDetector } returns true
        every { measurementSettings.fallSettings.inactivityDetectorTimeoutS } returns 1
        every { measurementSettings.fallSettings.inactivityDetectorThreshold } returns 3

        triggerFall()

        verify(exactly = 0) { testObj.notifyHealthEvent(any(), any(), any()) }
        verify(exactly = 1) { testObj.checkPostFallActivity(any()) }
    }

    private fun triggerFall() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 7.8f)
        val sensorEvent2 = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 1.4f)
        val sensorEvent3 = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 0f)
        for (i in 0..7) {
            linearAccelerationSensorObservable.onNext(sensorEvent3)
        }
        linearAccelerationSensorObservable.onNext(sensorEvent)
        linearAccelerationSensorObservable.onNext(sensorEvent2)
    }

    @Test
    fun testFallDetect_shouldNotNotify() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 0f)
        for (i in 0..40) {
            linearAccelerationSensorObservable.onNext(sensorEvent)
        }

        verify(exactly = 0) { testObj.notifyHealthEvent(any(), any(), any()) }
    }
}