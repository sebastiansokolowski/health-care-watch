package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.nhaarman.mockitokotlin2.*
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.SensorEventMock.Companion.getMockedSensorEventWrapper
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit


/**
 * Created by Sebastian Sokołowski on 17.09.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class HeartRateAnomalyEngineTest {

    private val healthSensorObservable: ReplaySubject<SensorEvent> = ReplaySubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @Spy
    var testObj: HeartRateAnomalyEngine = HeartRateAnomalyEngine()

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var measurementSettings: MeasurementSettings

    @Mock
    lateinit var stepDetector: StepDetector

    @Mock
    lateinit var sensorsObservable: SensorsObservable

    private val testScheduler = TestScheduler()

    @Before
    fun setUp() {
        whenever(sensorsObservable.heartRateObservable).doReturn(healthSensorObservable)
        whenever(stepDetector.isStepDetected()).doReturn(true)
        whenever(stepDetector.scheduler).doReturn(testScheduler)
        whenever(measurementSettings.heartRateAnomalySettings.activityDetectorThreshold).doReturn(5)
        whenever(measurementSettings.heartRateAnomalySettings.activityDetectorTimeoutMin).doReturn(5)
        whenever(measurementSettings.heartRateAnomalySettings.minThreshold).doReturn(40)
        whenever(measurementSettings.heartRateAnomalySettings.maxThresholdDuringInactivity).doReturn(120)
        whenever(measurementSettings.heartRateAnomalySettings.maxThresholdDuringActivity).doReturn(150)
        whenever(testObj.scheduler).doReturn(testScheduler)

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

        verify(testObj, times(1)).notifyHealthEvent(eq(sensorEvent), any(), any(), any())
    }

    @Test
    fun testTooLowHeartRate_shouldNotNotify() {
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        val sensorEvent = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 41f)
        val sensorEvent2 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 141f)
        val sensorEvent3 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 120f)
        val sensorEvent4 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 110f)
        val sensorEvent5 = getMockedSensorEventWrapper(Sensor.TYPE_HEART_RATE, value = 90f)
        whenever(stepDetector.isStepDetected()).doReturn(true)
        healthSensorObservable.onNext(sensorEvent)
        healthSensorObservable.onNext(sensorEvent2)
        healthSensorObservable.onNext(sensorEvent3)
        whenever(stepDetector.isStepDetected()).doReturn(false)
        healthSensorObservable.onNext(sensorEvent4)
        healthSensorObservable.onNext(sensorEvent5)

        verify(testObj, never()).notifyHealthEvent(any(), any(), any(), any())
    }
}