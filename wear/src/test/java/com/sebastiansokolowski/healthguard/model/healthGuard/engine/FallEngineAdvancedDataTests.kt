package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.FallSettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class FallEngineAdvancedDataTests : DataTestsBase() {

    private val fallPath = ""

    override fun getEventPath() = fallPath

    override fun createEngine() = FallEngineAdvanced()

    override fun getHealthEventType() = HealthEventType.FALL_ADVANCED

    @Test
    fun findTheBestFallSettings() {
        val thresholdValues = IntRange(20, 30).step(1).toList()
        val samplingTimeSValues = IntRange(6, 10).step(1).toList()
        val inactivityDetectorTimeoutSValues = IntRange(1, 5).step(1).toList()
        val inactivityDetectorThresholdValues = IntRange(1, 5).step(1).toList()

        var theBestSummaryTestResult: TestResultSummary? = null
        var theBestFallSettings: FallSettings? = null
        thresholdValues.forEach { threshold ->
            samplingTimeSValues.forEach { samplingTimeS ->
                inactivityDetectorTimeoutSValues.forEach { inactivityDetectorTimeoutS ->
                    inactivityDetectorThresholdValues.forEach { inactivityDetectorThreshold ->
                        val fallSettings = FallSettings(threshold, samplingTimeS,
                                true, inactivityDetectorTimeoutS, inactivityDetectorThreshold)
                        println("testing $fallSettings")

                        val measurementSettings = MeasurementSettings(fallSettings = fallSettings)
                        val summary = testFiles(measurementSettings)

                        if (theBestSummaryTestResult == null || theBestSummaryTestResult!!.getDetectionAccuracy() <= summary.getDetectionAccuracy()) {
                            theBestSummaryTestResult = summary
                            theBestFallSettings = fallSettings

                            println("found $theBestFallSettings$theBestSummaryTestResult")
                        }
                    }
                }
            }
        }

        println("\nThe best tests result $theBestSummaryTestResult" +
                "\nFall settings\n$theBestFallSettings")
    }

    @Test
    fun testFiles() {
        val fallSettings = FallSettings()
        val measurementSettings = MeasurementSettings(fallSettings = fallSettings)
        val summary = testFiles(measurementSettings)

        println("\nFall test results summary$summary\n")
    }

}