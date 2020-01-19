package com.sebastiansokolowski.healthcarewatch.model.healthCare.detector

import android.hardware.Sensor
import com.sebastiansokolowski.healthcarewatch.dataModel.HealthSensorEvent
import com.sebastiansokolowski.healthcarewatch.model.healthCare.SensorEventMock.Companion.getMockedSensorEventWrapper
import io.mockk.every
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
@ExtendWith(MockKExtension::class)
class StepDetectorTest {
    private val timeout: Long = 1000

    @SpyK
    var testObj = StepDetector(timeout)

    private val healthSensorObservable: PublishSubject<HealthSensorEvent> = PublishSubject.create()

    @BeforeEach
    fun setup() {
        testObj.setupDetector(healthSensorObservable)
        testObj.startDetector()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun isStepDetected_WhenNoEvent_shouldReturnFalse() {
        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenComeEvent_shouldReturnTrue() {
        healthSensorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))

        assertTrue(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenTimeout_shouldReturnFalse() {
        every { testObj.getCurrentTimestamp() } returns 0
        healthSensorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))
        every { testObj.getCurrentTimestamp() } returns timeout

        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun stopDetector_shouldReturnFalse() {
        testObj.stopDetector()
        healthSensorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

}