package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import android.hardware.Sensor
import com.google.gson.Gson
import com.sebastiansokolowski.healthguard.dataModel.SensorsObservable
import com.sebastiansokolowski.shared.dataModel.DataExport
import com.sebastiansokolowski.shared.dataModel.HealthEvent
import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.FallSettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class FallEngineAdvencedTest {

    private val extension = "json"
    private val path = "../../mHealth-Guard_data/"
    private val adlCount = 10

    private val cache = hashMapOf<File, DataExport>()

    private var testObj = FallEngineAdvanced()
    private var testScheduler = TestScheduler()
    private var notifyObservable: PublishSubject<HealthEvent> = PublishSubject.create()
    private var sensorsObservable = SensorsObservable()

    @Test
    fun findTheBestFallSettings() {
        val thresholdValues = IntRange(20, 30).step(1).toList()
        val samplingTimeSValues = IntRange(4, 6).step(1).toList()
        val inactivityDetectorTimeoutSValues = IntRange(1, 4).step(1).toList()
        val inactivityDetectorThresholdValues = IntRange(1, 10).step(1).toList()

        var theBestSummaryTestResult: TestResult? = null
        var theBestFallSettings: FallSettings? = null
        thresholdValues.forEach { threshold ->
            samplingTimeSValues.forEach { samplingTimeS ->
                inactivityDetectorTimeoutSValues.forEach { inactivityDetectorTimeoutS ->
                    inactivityDetectorThresholdValues.forEach { inactivityDetectorThreshold ->
                        val fallSettings = FallSettings(threshold, samplingTimeS,
                                true, inactivityDetectorTimeoutS, inactivityDetectorThreshold)
                        println("testing $fallSettings")

                        val testsResults = testAllFiles(MeasurementSettings(fallSettings = fallSettings))
                        val summaryTestResult = createSummary(testsResults)

                        if (theBestSummaryTestResult == null || theBestSummaryTestResult!!.detectionAccuracy <= summaryTestResult.detectionAccuracy) {
                            theBestSummaryTestResult = summaryTestResult
                            theBestFallSettings = fallSettings

                            println("found $theBestFallSettings$theBestSummaryTestResult")
                        }
                    }
                }
            }
        }

        println("\nThe best tests result $theBestSummaryTestResult" +
                "Fall settings\n$theBestFallSettings")
    }


    @Test
    fun testAllFiles() {
        val fallSettings = FallSettings(28, 6, true, 2, 3)
        val measurementSettings = MeasurementSettings(fallSettings = fallSettings)

        val testsResults = testAllFiles(measurementSettings)
        val summary = createSummary(testsResults)

        println("\nTest results summary$summary\n" +
                testsResults.toString())
    }

    private fun testAllFiles(measurementSettings: MeasurementSettings): MutableList<TestResult> {
        setupEngine(measurementSettings)

        val testResults = mutableListOf<TestResult>()
        val files = getAllTestFiles(File(path))
        files.forEach {
            testResults.add(testFile(it))
        }

        return testResults
    }

    private fun setupEngine(measurementSettings: MeasurementSettings) {
        notifyObservable = PublishSubject.create()
        testObj = FallEngineAdvanced()
        testScheduler = TestScheduler()
        sensorsObservable = SensorsObservable()

        testObj.scheduler = testScheduler
        testObj.setupEngine(sensorsObservable, notifyObservable, measurementSettings)
        testObj.startEngine()
    }

    private fun createSummary(testResults: MutableList<TestResult>): TestResult {
        val globalTestResults = TestResult(null, 1f, 1f, 1f,
                0,
                0, 0)
        testResults.forEach {
            globalTestResults.detectionAccuracy *= it.detectionAccuracy
            globalTestResults.fallDetectionAccuracy *= it.fallDetectionAccuracy
            globalTestResults.adlDetectionAccuracy *= it.adlDetectionAccuracy
            globalTestResults.measurementTime += it.measurementTime
            globalTestResults.expectedHealthEventCount += it.expectedHealthEventCount
            globalTestResults.actualHealthEventCount += it.actualHealthEventCount
        }

        return globalTestResults
    }

    private fun getAllTestFiles(file: File): MutableList<File> {
        val results = mutableListOf<File>()
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                results.addAll(getAllTestFiles(it))
            }
        }
        if (file.extension == extension) {
            results.add(file)
        }
        return results
    }

    private fun testFile(file: File): TestResult {
        val dataExport: DataExport = cache.getOrPut(file, { Gson().fromJson(FileReader(file), DataExport::class.java) })

        val notifiedHealthEvents = mutableSetOf<HealthEvent>()
        notifyObservable
                .subscribeOn(testScheduler)
                .subscribe {
                    notifiedHealthEvents.add(it)
                }
        val fallEventsCount = dataExport.healthEvents.count { it.healthEventType == HealthEventType.FALL_ADVANCED }

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

        //fall
        var fallDetectionAccuracy: Float
        var fall = 0
        val fallCount = dataExport.healthEvents.size
        dataExport.healthEvents.forEach { healthEvent ->
            val exist = notifiedHealthEvents.find { it.sensorEvent == healthEvent.sensorEvent }
            if (exist != null) {
                fall++
            }
        }
        fallDetectionAccuracy = if (fallCount == 0) {
            1f
        } else {
            fall / fallCount.toFloat()
        }

        //adl
        var adlDetectionAccuracy: Float
        var adl = 0
        val adlCount = adlCount
        notifiedHealthEvents.forEach { notifiedHealthEvent ->
            val exist = dataExport.healthEvents.find { it.sensorEvent == notifiedHealthEvent.sensorEvent }
            if (exist == null) {
                adl++
            }
        }
        adlDetectionAccuracy = if (adlCount == 0) {
            1f
        } else {
            1 - (adl / adlCount.toFloat())
        }
        //
        if (fallDetectionAccuracy < 0) {
            fallDetectionAccuracy = 0f
        }
        if (adlDetectionAccuracy < 0) {
            adlDetectionAccuracy = 0f
        }
        val detectionAccuracy = fallDetectionAccuracy * adlDetectionAccuracy
        return TestResult(file,
                detectionAccuracy, fallDetectionAccuracy, adlDetectionAccuracy,
                measurementTime, fallEventsCount, adl + fall)
    }

    data class TestResult(
            var file: File?,
            var detectionAccuracy: Float,
            var fallDetectionAccuracy: Float,
            var adlDetectionAccuracy: Float,
            var measurementTime: Long,
            var expectedHealthEventCount: Int,
            var actualHealthEventCount: Int
    ) {
        override fun toString(): String {
            var text = ""
            if (file != null) {
                text += "\nfile=$file"
            }
            text += "\n detectionAccuracy=${detectionAccuracy * 100}%" +
                    "\n fallDetectionAccuracy=${fallDetectionAccuracy * 100}%" +
                    "\n adlDetectionAccuracy=${adlDetectionAccuracy * 100}%" +
                    "\n measurementTimeInMinutes=${TimeUnit.MILLISECONDS.toMinutes(measurementTime)}" +
                    "\n expectedHealthEventCount=$expectedHealthEventCount" +
                    "\n actualHealthEventCount=$actualHealthEventCount"
            return text
        }
    }
}