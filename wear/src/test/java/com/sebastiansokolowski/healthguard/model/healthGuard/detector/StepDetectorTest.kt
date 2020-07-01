package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class StepDetectorTest {
    private val timeout: Long = 1000

    @Spy
    var testObj = StepDetector(timeout)

    private val stepDetectorObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    @Mock
    lateinit var sensorsObservable: SensorsObservable

    @Before
    fun setup() {
        whenever(sensorsObservable.stepDetectorObservable) doReturn (stepDetectorObservable)

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
        whenever(testObj.getCurrentTimestamp()).thenReturn(0)
        stepDetectorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))
        whenever(testObj.getCurrentTimestamp()).doReturn(timeout)

        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun stopDetector_shouldReturnFalse() {
        testObj.stopDetector()
        stepDetectorObservable.onNext(getMockedSensorEventWrapper(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

}