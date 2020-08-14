package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.healthguard.model.healthGuard.HealthGuardEngineBase
import com.sebastiansokolowski.shared.dataModel.DataExport
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileReader
import java.lang.Math.pow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

abstract class DataTestsBase {

    companion object {
        private val extension = "json"
        private val adlPath = ""

        private val adlTimeS = 60
        private val adlEventWeight = 2
    }

    private val cache = ConcurrentHashMap<File, DataExport>()

    abstract fun getEventPath(): String

    abstract fun createEngine(): HealthGuardEngineBase

    abstract fun getHealthEventType(): HealthEventType

    fun testFiles(measurementSettings: MeasurementSettings, showFileStatistics: Boolean = false): TestResultSummary {
        val eventTestsResults = testFiles(measurementSettings, File(getEventPath()), false)
        val adlTestsResults = testFiles(measurementSettings, File(adlPath), true)
        return createSummary(eventTestsResults, adlTestsResults, showFileStatistics)
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
            val delayTime = it.timestamp - lastTimestamp
            // put only data from test mode
            if (delayTime < 500) {
                sensorsObservable.linearAccelerationObservable.onNext(it)
                measurementTime += delayTime
            }
            testScheduler.advanceTimeBy(delayTime, TimeUnit.MILLISECONDS)
            lastTimestamp = it.timestamp
        }

        //events
        var eventsDetected = 0
        var eventsExpected = 0

        if (adlTest) {
            eventsDetected = notifiedHealthEvents.size
            eventsExpected = dataExport.counter
                    ?: TimeUnit.MILLISECONDS.toSeconds(measurementTime).toInt() / adlTimeS
        } else {
            eventsDetected = notifiedHealthEvents.map { it.sensorEvent }.toSet().size
            eventsExpected = dataExport.counter
                    ?: dataExport.healthEvents.count { it.healthEventType == getHealthEventType() }
        }


        val detectionAccuracy = if (adlTest) {
            if (eventsExpected == 0) {
                1f
            } else {
                1 - (eventsDetected / eventsExpected.toFloat())
            }
        } else {
            if (eventsExpected == 0) {
                1f
            } else {
                eventsDetected / eventsExpected.toFloat()
            }
        }

        return TestResult(file,
                adlTest,
                detectionAccuracy,
                measurementTime,
                eventsDetected,
                eventsExpected)
    }

    fun createSummary(fallTestsResults: MutableList<TestResult>, adlTestsResults: MutableList<TestResult>, showExtraLogs: Boolean): TestResultSummary {
        val testResultsSummary = TestResultSummary(
                0, 0, 0,
                0, 0, 0)

        fallTestsResults.forEach { fallTestsResult ->
            if (showExtraLogs) {
                println(fallTestsResult)
            }
            testResultsSummary.apply {
                eventsDetected += fixEventDetected(fallTestsResult)
//                eventsDetected += fallTestsResult.eventsDetected
                eventsExpected += fallTestsResult.eventsExpected
                eventsMeasurementTime += fallTestsResult.measurementTime
            }
        }
        adlTestsResults.forEach { adlTestsResult ->
            if (showExtraLogs) {
                println(adlTestsResult)
            }
            testResultsSummary.apply {
                adlEventsDetected += fixEventDetected(adlTestsResult)
                adlEventsExpected += adlTestsResult.eventsExpected
                adlMeasurementTime += adlTestsResult.measurementTime
            }
        }
        return testResultsSummary
    }

    private fun fixEventDetected(testResult: TestResult): Int {
        //handle the case when we detected more events than expected
        testResult.apply {
            var fixedEventsDetected = eventsDetected
            if (fixedEventsDetected > eventsExpected) {
                fixedEventsDetected -= (eventsExpected - fixedEventsDetected)
            }
            if (fixedEventsDetected > eventsExpected) {
                fixedEventsDetected = 0
            }
            return fixedEventsDetected
        }
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
            var eventsDetected: Int,
            var eventsExpected: Int,
            var eventsMeasurementTime: Long,
            var adlEventsDetected: Int,
            var adlEventsExpected: Int,
            var adlMeasurementTime: Long
    ) {

        fun getEventsDetectionAccuracy(): Float {
            return eventsDetected / eventsExpected.toFloat()
        }

        fun getAdlDetectionAccuracy(): Float {
            return (adlEventsExpected - adlEventsDetected) / adlEventsExpected.toFloat()
        }

        fun getDetectionAccuracy(): Float {
            return (getEventsDetectionAccuracy() + getAdlDetectionAccuracy()) / 2
        }

        fun getWeightDetectionAccuracy(): Float {
            return (getEventsDetectionAccuracy() + pow(getAdlDetectionAccuracy().toDouble(), adlEventWeight.toDouble()).toFloat()) / 2
        }

        override fun toString(): String {
            return "\n detectionAccuracy=${getDetectionAccuracy() * 100}%" +
                    "\n weightDetectionAccuracy=${getWeightDetectionAccuracy() * 100}%" +
                    "\n eventsDetectionAccuracy=${getEventsDetectionAccuracy() * 100}%" +
                    "\n $eventsDetected/$eventsExpected" +
                    "\n eventsMeasurementTime=${TimeUnit.MILLISECONDS.toMinutes(eventsMeasurementTime)}" +
                    "\n adlDetectionAccuracy=${getAdlDetectionAccuracy() * 100}%" +
                    "\n $adlEventsDetected/$adlEventsExpected" +
                    "\n adlMeasurementTime=${TimeUnit.MILLISECONDS.toMinutes(adlMeasurementTime)}"
        }

    }
}