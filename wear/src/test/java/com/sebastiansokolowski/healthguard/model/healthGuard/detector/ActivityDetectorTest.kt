package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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
class ActivityDetectorTest {

    private val linearAccelerationObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    private val activityThreshold: Int = 3
    private val bufferTime: Long = 10

    @SpyK
    var testObj = ActivityDetector(activityThreshold, bufferTime)

    @MockK
    lateinit var sensorsObservable: SensorsObservable

    @BeforeEach
    fun setup() {
        every { sensorsObservable.linearAccelerationObservable } returns linearAccelerationObservable
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