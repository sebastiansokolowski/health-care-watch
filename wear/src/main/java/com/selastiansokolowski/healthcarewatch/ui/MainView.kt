package com.selastiansokolowski.healthcarewatch.ui

/**
 * Created by Sebastian Sokołowski on 09.07.18.
 */
interface MainView {

    fun setMeasurementState(running: Boolean)

    fun setHearthRate(heartRate: String)

}