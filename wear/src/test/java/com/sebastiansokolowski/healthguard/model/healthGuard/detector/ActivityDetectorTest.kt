package com.sebastiansokolowski.healthguard.model.healthGuard.detector

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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
class ActivityDetectorTest {

    private val healthSensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()

    private val activityThreshold: Int = 3
    private val bufferTime: Long = 1

    @SpyK
    var testObj = ActivityDetector(activityThreshold, bufferTime)

    @BeforeEach
    fun setup() {
        testObj.setupDetector(healthSensorObservable)
        testObj.startDetector()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun isActivityDetected_WhenActivityIsLowerThanThreshold_shouldReturnFalse() {
        val values = floatArrayOf(1.7f, 1.7f, 1.7f)
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
        for (i in 0..10) {
            healthSensorObservable.onNext(sensorEvent)
        }

        assertFalse(testObj.activityStateObservable.blockingFirst())
    }

    @Test
    fun isActivityDetected_WhenActivityIsHigherThanThreshold_shouldReturnTrue() {
        val values = floatArrayOf(1.8f, 1.8f, 1.8f)
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
        for (i in 0..10) {
            healthSensorObservable.onNext(sensorEvent)
        }

        assertTrue(testObj.activityStateObservable.blockingFirst())
    }

    @Test
    fun activityStateObservable_shouldReturnOnlyOneItem() {
        val values = floatArrayOf(1.7f, 1.7f, 1.7f)
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_LINEAR_ACCELERATION, values = values)
        for (i in 0..100) {
            healthSensorObservable.onNext(sensorEvent)
        }
        testObj.activityStateObservable.blockingFirst()
        for (i in 0..100) {
            healthSensorObservable.onNext(sensorEvent)
        }

        verify(exactly = 1) { testObj.notifyActivityState(any()) }
    }
}