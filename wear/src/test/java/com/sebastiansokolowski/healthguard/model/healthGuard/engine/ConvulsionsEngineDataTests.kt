package com.sebastiansokolowski.healthguard.model.healthGuard.engine

import com.sebastiansokolowski.shared.dataModel.HealthEventType
import com.sebastiansokolowski.shared.dataModel.settings.ConvulsionsSettings
import com.sebastiansokolowski.shared.dataModel.settings.MeasurementSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Created by Sebastian Sokołowski on 04.11.19.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class ConvulsionsEngineDataTests : DataTestsBase() {

    private val eventPath = ""

    override fun getEventPath() = eventPath

    override fun createEngine() = ConvulsionsEngine()

    override fun getHealthEventType() = HealthEventType.CONVULSIONS

    @Test
    fun findTheBestConvulsionsSettings() {
        val thresholdValues = IntRange(3, 10).step(1).toList()
        val samplingTimeSValues = IntRange(5, 15).step(1).toList()
        val motionsValues = IntRange(30, 150).step(5).toList()
        val motionsToCancelValues = IntRange(0, 50).step(5).toList()

        val executorService = Executors.newScheduledThreadPool(5)
        val numberOfOptions = thresholdValues.size * samplingTimeSValues.size *
                motionsValues.size * motionsToCancelValues.size
        val countDownLatch = CountDownLatch(numberOfOptions)

        var theBestSummaryTestResult: TestResultSummary? = null
        var theBestConvulsionsSettings: ConvulsionsSettings? = null
        thresholdValues.forEach { threshold ->
            samplingTimeSValues.forEach { samplingTimeS ->
                motionsValues.forEach { motions ->
                    motionsToCancelValues.forEach { motionsToCancel ->
                        executorService.submit {
                            val convulsionsSettings = ConvulsionsSettings(threshold, samplingTimeS, motions, motionsToCancel)
                            println("testing $convulsionsSettings")

                            val measurementSettings = MeasurementSettings(convulsionsSettings = convulsionsSettings)
                            val summary = testFiles(measurementSettings)

                            synchronized(this) {
                                if (theBestSummaryTestResult == null || theBestSummaryTestResult!!.getWeightDetectionAccuracy() <= summary.getWeightDetectionAccuracy()) {
                                    theBestSummaryTestResult = summary
                                    theBestConvulsionsSettings = convulsionsSettings

                                    println("found $theBestConvulsionsSettings$theBestSummaryTestResult")
                                }
                            }
                            countDownLatch.countDown()
                        }
                    }
                }
            }
        }
        countDownLatch.await()

        println("\nThe best tests result $theBestSummaryTestResult" +
                "\nConvulsions settings\n$theBestConvulsionsSettings")
    }

    @Test
    fun testFiles() {
        val convulsionsSettings = ConvulsionsSettings()
        val measurementSettings = MeasurementSettings(convulsionsSettings = convulsionsSettings)
        val summary = testFiles(measurementSettings, true)

        println("\nConvulsions test results summary$summary\n")
    }

}