package com.selastiansokolowski.healthcarewatch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.BoxInsetLayout
import android.widget.Button
import android.widget.TextView


class MainActivity : WearableActivity(), SensorEventListener {

    private var mContainerView: BoxInsetLayout? = null
    private var mTextView: TextView? = null
    private var mStartButton: Button? = null

    private var mSensorManager: SensorManager? = null
    private var mHeartRateSensor: Sensor? = null

    private var measureStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        mContainerView = findViewById(R.id.container) as BoxInsetLayout
        mTextView = findViewById(R.id.text) as TextView
        mStartButton = findViewById(R.id.start_btn) as Button
        mStartButton?.setOnClickListener { startMeasure() }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mHeartRateSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    fun startMeasure(){
        mTextView?.setText("");


        var sensorRegistered = mSensorManager?.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)

        if(sensorRegistered == true){
            measureStarted = true
            mStartButton?.setText("Stop")
        }else{
            measureStarted = false
            mStartButton?.setText("Start")

            mSensorManager?.unregisterListener(this);
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mSensorManager?.unregisterListener(this);
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
         //TODO:
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val mHeartRateFloat = event!!.values[0]

        val mHeartRate = Math.round(mHeartRateFloat)

        mTextView?.setText(Integer.toString(mHeartRate))
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
        updateDisplay()
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
        updateDisplay()
    }

    override fun onExitAmbient() {
        updateDisplay()
        super.onExitAmbient()
    }

    private fun updateDisplay() {
        if (isAmbient) {
            mContainerView!!.setBackgroundColor(resources.getColor(android.R.color.black))
            mTextView!!.setTextColor(resources.getColor(android.R.color.white))
        } else {
            mContainerView!!.background = null
            mTextView!!.setTextColor(resources.getColor(android.R.color.black))
        }
    }

}
