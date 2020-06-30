package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import com.sebastiansokolowski.healthguard.model.SensorDataModel
import com.sebastiansokolowski.healthguard.model.healthGuard.detector.StepDetector
import com.sebastiansokolowski.shared.dataModel.DataExport
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.SensorEvent
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner::class)
class FallEngineTest3 {

    private val linearAccelerationObservable: PublishSubject<SensorEvent> = PublishSubject.create()
    private val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()

    @Spy
    lateinit var testObj: FallEngineAdvanced

    lateinit var measurementSettings: MeasurementSettings

    @Mock
    lateinit var sensorDataModel: SensorDataModel

    @Mock
    lateinit var stepDetector: StepDetector

    lateinit var dataExport: DataExport

    lateinit var testScheduler: TestScheduler

    @Before
    fun setUp() {
        measurementSettings = MeasurementSettings()
        testScheduler = TestScheduler()

        whenever(sensorDataModel.linearAccelerationObservable).thenReturn(linearAccelerationObservable)
        whenever(stepDetector.isStepDetected()).thenReturn(true)
        whenever(testObj.scheduler()).thenReturn(testScheduler)

        testObj.setupEngine(sensorDataModel, notifyObservable, MeasurementSettings())
        testObj.stepDetector = stepDetector
        testObj.startEngine()

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    fun getAllFiles() {
        val file = File("../../mHealth-Guard_data/")
    }

    @Test
    fun loadData() {
        val file = File("../../mHealth-Guard_data/fall_28.06.2020.json")
        val export = Gson().fromJson(FileReader(file), DataExport::class.java)

        val events = mutableSetOf<SensorEvent>()
        doAnswer {
            events.add(it.arguments[0] as SensorEvent)
        }.whenever(testObj).notifyHealthEvent(any(), any(), any(), any())

        val fallEventsCount = export.healthEvents.count { it.healthEventType == HealthEventType.FALL_ADVANCED }

        val sortedEvents = export.sensorEvents.sortedBy { it.timestamp }

        var lastTimestamp = export.sensorEvents.first().timestamp
        sortedEvents.forEachIndexed { i, it ->
            println("$i/${sortedEvents.size}")
            linearAccelerationObservable.onNext(it)
            val delay = it.timestamp - lastTimestamp
            testScheduler.advanceTimeBy(delay, TimeUnit.MILLISECONDS)
            lastTimestamp = it.timestamp
        }
        assertEquals(fallEventsCount - 3, events.size)
    }

}