package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.nhaarman.mockitokotlin2.*
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class FallEngineTest {

    private val linearAccelerationSensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @Spy
    var testObj = FallEngine()

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var measurementSettings: MeasurementSettings

    @Mock
    lateinit var stepDetector: StepDetector

    @Mock
    lateinit var sensorsObservable: SensorsObservable

    @Before
    fun setUp() {
        whenever(sensorsObservable.linearAccelerationObservable).doReturn(linearAccelerationSensorObservable)

        whenever(stepDetector.isStepDetected()).doReturn(true)
        whenever(measurementSettings.fallSettings.threshold).doReturn(2)
        whenever(measurementSettings.fallSettings.stepDetector).doReturn(true)
        whenever(measurementSettings.fallSettings.stepDetectorTimeoutS).doReturn(10)
        whenever(measurementSettings.fallSettings.inactivityDetector).doReturn(false)
        whenever(measurementSettings.fallSettings.samplingTimeS).doReturn(10)
        whenever(measurementSettings.measurementId).doReturn(1)

        testObj.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        testObj.stepDetector = stepDetector
        testObj.startEngine()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testFallDetect_shouldNotify() {
        triggerFall()

        verify(testObj, times(1)).notifyHealthEvent(any(), any(), any(), any())
    }

    @Test
    fun testFallDetect_WhenStepIsNotDetected_shouldNotNotify() {
        whenever(stepDetector.isStepDetected()).doReturn(false)
        triggerFall()

        verify(testObj, never()).notifyHealthEvent(any(), any(), any(), any())
    }

    @Test
    fun testFallDetect_WhenStepDetectIsDisabled_shouldNotify() {
        whenever(stepDetector.isStepDetected()).doReturn(false)
        whenever(measurementSettings.fallSettings.stepDetector).doReturn(false)

        triggerFall()

        verify(testObj, times(1)).notifyHealthEvent(any(), any(), any(), any())
    }

    @Test
    fun testFallDetect_WhenPostFallDetectionIsEnabled_shouldNotNotify() {
        whenever(measurementSettings.fallSettings.inactivityDetector).doReturn(true)
        whenever(measurementSettings.fallSettings.inactivityDetectorTimeoutS).doReturn(1)
        whenever(measurementSettings.fallSettings.inactivityDetectorThreshold).doReturn(3)

        triggerFall()

        verify(testObj, never()).notifyHealthEvent(any(), any(), any(), any())
        verify(testObj, times(1)).checkPostFallActivity(any())
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

        verify(testObj, never()).notifyHealthEvent(any(), any(), any(), any())
    }
}