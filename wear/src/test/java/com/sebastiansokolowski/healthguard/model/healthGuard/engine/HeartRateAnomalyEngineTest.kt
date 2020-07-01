package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 17.09.19.
 */
@ExtendWith(MockKExtension::class)
class HeartRateAnomalyEngineTest {

    private val healthSensorObservable: ReplaySubject<SensorEvent> = ReplaySubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @SpyK
    var testObj: HeartRateAnomalyEngine = HeartRateAnomalyEngine()

    @RelaxedMockK
    lateinit var measurementSettings: MeasurementSettings

    @RelaxedMockK
    lateinit var stepDetector: StepDetector

    @MockK
    lateinit var sensorsObservable: SensorsObservable

    private val testScheduler = TestScheduler()

    @BeforeEach
    fun setUp() {
        every { sensorsObservable.heartRateObservable } returns healthSensorObservable
        every { stepDetector.isStepDetected() } returns true
        every { stepDetector.scheduler } returns testScheduler
        every { measurementSettings.heartRateAnomalySettings.activityDetectorThreshold } returns 5
        every { measurementSettings.heartRateAnomalySettings.activityDetectorTimeoutMin } returns 5
        every { measurementSettings.heartRateAnomalySettings.minThreshold } returns 40
        every { measurementSettings.heartRateAnomalySettings.maxThresholdDuringInactivity } returns 120
        every { measurementSettings.heartRateAnomalySettings.maxThresholdDuringActivity } returns 150
        every { testObj.scheduler } returns testScheduler

        testObj.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        testObj.stepDetector = stepDetector
        testObj.startEngine()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testTooLowHeartRate_shouldNotify() {
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 30f)
        healthSensorObservable.onNext(sensorEvent)

        verify(exactly = 1) { testObj.notifyHealthEvent(sensorEvent, any(), any(), any()) }
    }

    @Test
    fun testTooLowHeartRate_shouldNotNotify() {
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 41f)
        val sensorEvent2 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 141f)
        val sensorEvent3 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 120f)
        val sensorEvent4 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 110f)
        val sensorEvent5 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 90f)
        every { stepDetector.isStepDetected() } returns true
        healthSensorObservable.onNext(sensorEvent)
        healthSensorObservable.onNext(sensorEvent2)
        healthSensorObservable.onNext(sensorEvent3)
        every { stepDetector.isStepDetected() } returns false
        healthSensorObservable.onNext(sensorEvent4)
        healthSensorObservable.onNext(sensorEvent5)

        verify(exactly = 0) { testObj.notifyHealthEvent(any(), any(), any()) }
    }
}