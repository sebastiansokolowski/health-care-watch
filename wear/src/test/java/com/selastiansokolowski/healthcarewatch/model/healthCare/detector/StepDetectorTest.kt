package com.selastiansokolowski.healthcarewatch.model.healthCare.detector

import android.hardware.Sensor
import android.hardware.SensorEvent
import com.selastiansokolowski.healthcarewatch.model.healthCare.SensorEventMock.Companion.getMockedSensorEvent
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito

/**
 * Created by Sebastian Sokołowski on 23.09.19.
 */
class StepDetectorTest {
    private lateinit var testObj: StepDetector

    private val sensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    @Before
    fun setup() {
        testObj = Mockito.spy(StepDetector(1 * 1000))
        testObj.setupDetector(sensorObservable)
        testObj.startDetector()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun isStepDetected_WhenNoEvent_shouldReturnFalse() {
        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenOldEvent_shouldReturnFalse() {
        sensorObservable.onNext(getMockedSensorEvent(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenComeEvent_shouldReturnTrue() {
        Mockito.`when`(testObj.timeout(anyLong())).thenReturn(true)
        sensorObservable.onNext(getMockedSensorEvent(Sensor.TYPE_STEP_DETECTOR))

        assertTrue(testObj.isStepDetected())
    }

    @Test
    fun isStepDetected_WhenTimeout_shouldReturnFalse() {
        Mockito.`when`(testObj.timeout(anyLong())).thenReturn(false)
        sensorObservable.onNext(getMockedSensorEvent(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

    @Test
    fun stopDetector_shouldReturnFalse() {
        testObj.stopDetector()
        sensorObservable.onNext(getMockedSensorEvent(Sensor.TYPE_STEP_DETECTOR))

        assertFalse(testObj.isStepDetected())
    }

}