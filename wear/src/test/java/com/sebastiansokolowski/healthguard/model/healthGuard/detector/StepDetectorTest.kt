package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
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

    private val stepDetectorObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    @MockK
    lateinit var sensorsObservable: SensorsObservable

    @BeforeEach
    fun setup() {
        every { sensorsObservable.stepDetectorObservable } returns stepDetectorObservable

        testObj.setupDetector(sensorsObservable)
        testObj.startDetector()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun isStepDetected_WhenNoEvent_shouldReturnFalse() {
        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenComeEvent_shouldReturnTrue() {
        stepDetectorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))

        assertTrue(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenTimeout_shouldReturnFalse() {
        every { testObj.getCurrentTimestamp() } returns 0
        stepDetectorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))
        every { testObj.getCurrentTimestamp() } returns timeout

        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun stopDetector_shouldReturnFalse() {
        testObj.stopDetector()
        stepDetectorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

}