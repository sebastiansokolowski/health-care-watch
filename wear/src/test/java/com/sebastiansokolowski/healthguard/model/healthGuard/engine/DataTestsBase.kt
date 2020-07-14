package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.DataExport
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

abstract class DataTestsBase {
    private val extension = "json"
    private val adlPath = ""

    private val adlTimeS = 60

    private val cache = ConcurrentHashMap<File, DataExport>()

    abstract fun getEventPath(): String

    abstract fun createEngine(): HealthGuardEngineBase

    abstract fun getHealthEventType(): HealthEventType

    fun testFiles(measurementSettings: MeasurementSettings): TestResultSummary {
        val eventTestsResults = testFiles(measurementSettings, File(getEventPath()), false)
        val adlTestsResults = testFiles(measurementSettings, File(adlPath), true)
        return createSummary(eventTestsResults, adlTestsResults)
    }

    private fun testFiles(measurementSettings: MeasurementSettings, path: File, adlTest: Boolean): MutableList<TestResult> {
        val testResults = mutableListOf<TestResult>()
        val files = getTestFiles(path)
        files.forEach {
            testResults.add(testFile(it, measurementSettings, adlTest))
        }

        return testResults
    }

    private fun getTestFiles(file: File): MutableList<File> {
        val results = mutableListOf<File>()
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                results.addAll(getTestFiles(it))
            }
        }
        if (file.extension == extension) {
            results.add(file)
        }
        return results
    }

    private fun testFile(file: File, measurementSettings: MeasurementSettings, adlTest: Boolean): TestResult {
        //setup
        val testObj = createEngine()
        val testScheduler = TestScheduler()
        val notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()
        val sensorsObservable = SensorsObservable()

        testObj.scheduler = testScheduler
        testObj.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        testObj.startEngine()
        val dataExport: DataExport = cache.getOrPut(file, { Gson().fromJson(FileReader(file), DataExport::class.java) })

        //test
        val notifiedHealthEvents = mutableSetOf<HealthEvent>()
        notifyObservable
                .subscribeOn(testScheduler)
                .debounce(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    notifiedHealthEvents.add(it)
                }

        val linearAccelerationSensorEvents = dataExport.sensorEvents
                .filter { it.type == Sensor.TYPE_LINEAR_ACCELERATION }
                .sortedBy { it.timestamp }
        var measurementTime = 0L
        var lastTimestamp = dataExport.sensorEvents.first().timestamp
        linearAccelerationSensorEvents.forEach {
            sensorsObservable.linearAccelerationObservable.onNext(it)
            val delayTime = it.timestamp - lastTimestamp
            if (delayTime < 2000) {
                measurementTime += delayTime
            }
            testScheduler.advanceTimeBy(delayTime, TimeUnit.MILLISECONDS)
            lastTimestamp = it.timestamp
        }

        //events
        var events = 0
        var eventsCount = 0

        if (adlTest) {
            events = notifiedHealthEvents.size
            eventsCount = dataExport.counter
                    ?: TimeUnit.MILLISECONDS.toSeconds(measurementTime).toInt() / adlTimeS
        } else {
            events = notifiedHealthEvents.map { it.sensorEvent }.toSet().size
            eventsCount = dataExport.counter
                    ?: dataExport.healthEvents.count { it.healthEventType == getHealthEventType() }
        }

        var detectionAccuracy = if (adlTest) {
            if (eventsCount == 0) {
                1f
            } else {
                1 - (events / eventsCount.toFloat())
            }
        } else {
            if (eventsCount == 0) {
                1f
            } else {
                events / eventsCount.toFloat()
            }
        }
        //
        if (detectionAccuracy > 1) {
            detectionAccuracy = 1 - (detectionAccuracy - 1)
        }
        if (detectionAccuracy < 0 || detectionAccuracy > 1) {
            detectionAccuracy = 0f
        }

        return TestResult(file,
                adlTest,
                detectionAccuracy,
                measurementTime,
                events,
                eventsCount)
    }

    fun createSummary(fallTestsResults: MutableList<TestResult>, adlTestsResults: MutableList<TestResult>): TestResultSummary {
        val testResultsSummary = TestResultSummary(
                0f, 0, 0,
                0f, 0, 0)

        fallTestsResults.forEach { fallTestsResult ->
//            println(fallTestsResult)
            testResultsSummary.apply {
                eventsDetectionAccuracy += fallTestsResult.detectionAccuracy
                eventsDetected += fallTestsResult.eventsDetected
                eventsExpected += fallTestsResult.eventsExpected
            }
        }
        adlTestsResults.forEach { adlTestsResult ->
//            println(adlTestsResult)
            testResultsSummary.apply {
                adlDetectionAccuracy += adlTestsResult.detectionAccuracy
                adlEventsDetected += adlTestsResult.eventsDetected
                adlEventsExpected += adlTestsResult.eventsExpected
            }
        }
        testResultsSummary.eventsDetectionAccuracy /= fallTestsResults.size
        testResultsSummary.adlDetectionAccuracy /= adlTestsResults.size
        return testResultsSummary
    }

    data class TestResult(
            var file: File?,
            val adlTest: Boolean,
            var detectionAccuracy: Float,
            var measurementTime: Long,
            var eventsDetected: Int,
            var eventsExpected: Int
    ) {
        override fun toString(): String {
            var text = "\n "
            text += if (adlTest) {
                "adl"
            } else {
                "event"
            }
            text += "\n file=$file" +
                    "\n detectionAccuracy=${detectionAccuracy * 100}%" +
                    "\n {$eventsDetected/$eventsExpected}" +
                    "\n measurementTime=$measurementTime"

            return text
        }
    }

    data class TestResultSummary(
            var eventsDetectionAccuracy: Float,
            var eventsDetected: Int,
            var eventsExpected: Int,
            var adlDetectionAccuracy: Float,
            var adlEventsDetected: Int,
            var adlEventsExpected: Int
    ) {

        fun getDetectionAccuracy(): Float {
            return (eventsDetectionAccuracy + adlDetectionAccuracy) / 2
        }

        override fun toString(): String {
            return "\n detectionAccuracy=${getDetectionAccuracy() * 100}%" +
                    "\n eventsDetectionAccuracy=${eventsDetectionAccuracy * 100}%" +
                    "\n $eventsDetected/$eventsExpected" +
                    "\n adlDetectionAccuracy=${adlDetectionAccuracy * 100}%" +
                    "\n $adlEventsDetected/$adlEventsExpected"
        }
    }
}