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
class ActivityDetectorTest {

    private val linearAccelerationObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    private val activityThreshold: Int = 3
    private val bufferTime: Long = 10

    @Spy
    var testObj = ActivityDetector(activityThreshold, bufferTime)

    @Mock
    lateinit var sensorsObservable: SensorsObservable

    @Before
    fun setup() {
        whenever(sensorsObservable.linearAccelerationObservable) doReturn (linearAccelerationObservable)
        testObj.setupDetector(sensorsObservable)
        testObj.startDetector()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun isActivityDetected_WhenActivityIsLowerThanThreshold_shouldReturnFalse() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 1.7f)
        for (i in 0..10) {
            linearAccelerationObservable.onNext(sensorEvent)
        }

        assertFalse(testObj.isActivityDetected())
    }

    @Test
    fun isActivityDetected_WhenActivityIsHigherThanThreshold_shouldReturnTrue() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, value = 4f)
        for (i in 0..10) {
            linearAccelerationObservable.onNext(sensorEvent)
        }

        assertTrue(testObj.isActivityDetected())
    }

}