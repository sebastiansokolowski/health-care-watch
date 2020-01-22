package com.sebastiansokolowski.healthcarewatch.model.healthCare.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthCareEvent
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.healthcarewatch.model.healthCare.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthcarewatch.model.healthCare.detector.StepDetector
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

    private val healthSensorObservable: PublishSubject<HealthSensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthCareEvent> = PublishSubject.create()

    @SpyK
    var testObj = FallEngine()

    @MockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var stepDetector: StepDetector

    @BeforeEach
    fun setUp() {
        testObj.stepDetector = stepDetector
        testObj.setupEngine(healthSensorObservable, notifyObservable, measurementSettings)
        testObj.startEngine()

        every { stepDetector.isStepDetected() } returns true
        every { measurementSettings.fallSettings.stepDetector } returns true
        every { measurementSettings.fallSettings.threshold } returns 2
        every { measurementSettings.fallSettings.timeOfInactivity } returns 0

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testFallDetect_shouldNotify() {
        triggerFall()

        verify(exactly = 1) { testObj.notifyHealthCareEvent(any()) }
    }

    @Test
    fun testFallDetect_WhenStepIsNotDetected_shouldNotNotify() {
        every { stepDetector.isStepDetected() } returns false
        triggerFall()

        verify(exactly = 0) { testObj.notifyHealthCareEvent(any()) }
    }

    @Test
    fun testFallDetect_WhenStepDetectIsDisabled_shouldNotify() {
        every { stepDetector.isStepDetected() } returns false
        every { measurementSettings.fallSettings.stepDetector } returns false

        triggerFall()

        verify(exactly = 1) { testObj.notifyHealthCareEvent(any()) }
    }

    @Test
    fun testFallDetect_WhenPostFallDetectionIsEnabled_shouldNotNotify() {
        every { measurementSettings.fallSettings.activityThreshold } returns 3
         every { measurementSettings.fallSettings.timeOfInactivity } returns 5

        triggerFall()

        verify(exactly = 0) { testObj.notifyHealthCareEvent(any()) }
        verify(exactly = 1) { testObj.checkPostFallActivity(any()) }
    }

    private fun triggerFall() {
        val values = floatArrayOf(-7.8f, 2.8f, -14.8f)
        val values2 = floatArrayOf(1.4f, 0.2f, -5.6f)
        val values3 = floatArrayOf(0f, 0f, 0f)

        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
        val sensorEvent2 = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values2)
        val sensorEvent3 = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values3)
        for (i in 0..7) {
            healthSensorObservable.onNext(sensorEvent3)
        }
        healthSensorObservable.onNext(sensorEvent)
        healthSensorObservable.onNext(sensorEvent2)
    }

    @Test
    fun testFallDetect_shouldNotNotify() {
        val values = floatArrayOf(0f, 0f, 0f)
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
        for (i in 0..40) {
            healthSensorObservable.onNext(sensorEvent)
        }

        verify(exactly = 0) { testObj.notifyHealthCareEvent(any()) }
    }
}