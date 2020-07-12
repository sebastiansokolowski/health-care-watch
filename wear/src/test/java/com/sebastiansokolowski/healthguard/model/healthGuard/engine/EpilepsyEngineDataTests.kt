package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.EpilepsySettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class EpilepsyEngineDataTests : DataTestsBase() {

    private val eventPath = ""

    override fun getEventPath() = eventPath

    override fun createEngine() = EpilepsyEngine()

    override fun getHealthEventType() = HealthEventType.EPILEPSY

    @Test
    fun findTheBestEpilepsySettings() {
        val thresholdValues = IntRange(3, 15).step(1).toList()
        val samplingTimeSValues = IntRange(5, 10).step(1).toList()
        val percentOfPositiveSignals = IntRange(30, 100).step(5).toList()

        var theBestSummaryTestResult: TestResultSummary? = null
        var theBestSeizureSettings: EpilepsySettings? = null
        thresholdValues.forEach { threshold ->
            samplingTimeSValues.forEach { samplingTimeS ->
                percentOfPositiveSignals.forEach { percentOfPositiveSignals ->
                    val epilepsySettings = EpilepsySettings(threshold, samplingTimeS, percentOfPositiveSignals)
                    println("testing $epilepsySettings")

                    val measurementSettings = MeasurementSettings(epilepsySettings = epilepsySettings)
                    val summary = testFiles(measurementSettings)

                    if (theBestSummaryTestResult == null || theBestSummaryTestResult!!.getDetectionAccuracy() <= summary.getDetectionAccuracy()) {
                        theBestSummaryTestResult = summary
                        theBestSeizureSettings = epilepsySettings

                        println("found $theBestSeizureSettings$theBestSummaryTestResult")
                    }
                }
            }
        }

        println("\nThe best tests result $theBestSummaryTestResult" +
                "\nEpilepsy settings\n$theBestSeizureSettings")
    }

    @Test
    fun testFiles() {
        val epilepsySettings = EpilepsySettings()
        val measurementSettings = MeasurementSettings(epilepsySettings = epilepsySettings)
        val summary = testFiles(measurementSettings)

        println("\nEpilepsy test results summary$summary\n")
    }

}