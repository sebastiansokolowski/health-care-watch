package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.ActivityDetector
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.mockk.every
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
 * Created by Sebastian Sokołowski on 17.09.19.
 */
@ExtendWith(MockKExtension::class)
class HeartRateAnomalyEngineTest {

    private val healthSensorObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @SpyK
    var testObj: HeartRateAnomalyEngine = HeartRateAnomalyEngine()

    @RelaxedMockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var activityDetector: ActivityDetector

    @BeforeEach
    fun setUp() {
        every { activityDetector.isActivityDetected() } returns true
        every { measurementSettings.heartRateAnomalySettings.activityDetectorThreshold } returns 5
        every { measurementSettings.heartRateAnomalySettings.activityDetectorTimeoutMin } returns 5
        every { measurementSettings.heartRateAnomalySettings.minThreshold } returns 40
        every { measurementSettings.heartRateAnomalySettings.maxThresholdDuringInactivity } returns 120
        every { measurementSettings.heartRateAnomalySettings.maxThresholdDuringActivity } returns 150

        testObj.setupEngine(healthSensorObservable, notifyObservable, measurementSettings)
        testObj.activityDetector = activityDetector
        testObj.startEngine()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testTooLowHeartRate_shouldNotify() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(30f))
        healthSensorObservable.onNext(sensorEvent)

        verify(exactly = 1) { testObj.notifyHealthEvent(sensorEvent, any(), any()) }
    }

    @Test
    fun testTooLowHeartRate_shouldNotNotify() {
        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(41f))
        val sensorEvent2 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(141f))
        val sensorEvent3 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(120f))
        val sensorEvent4 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(110f))
        val sensorEvent5 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, values = floatArrayOf(90f))
        every { activityDetector.isActivityDetected() } returns true
        healthSensorObservable.onNext(sensorEvent)
        healthSensorObservable.onNext(sensorEvent2)
        healthSensorObservable.onNext(sensorEvent3)
        every { activityDetector.isActivityDetected() } returns false
        healthSensorObservable.onNext(sensorEvent4)
        healthSensorObservable.onNext(sensorEvent5)

        verify(exactly = 0) { testObj.notifyHealthEvent(any(), any(), any()) }
    }
}