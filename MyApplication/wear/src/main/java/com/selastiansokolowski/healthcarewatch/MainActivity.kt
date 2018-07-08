package com.selastiansokolowski.healthcarewatch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.BoxInsetLayout
import android.widget.Button
import android.widget.TextView
import java.text.DecimalFormat


class MainActivity : WearableActivity(), SensorEventListener {

    private var measureStarted: Boolean = false

    private var mSensorManager: SensorManager? = null

    //View
    private lateinit var mContainerView: BoxInsetLayout
    private lateinit var mStartButton: Button
    private lateinit var mStepStatus: TextView
    private lateinit var mHearthRate: TextView
    private lateinit var mLinearAcceleration: TextView
    private lateinit var mAcceleration: TextView
    private lateinit var mGyroscope: TextView

    //Sensors
    private var mHeartRateSensor: Sensor? = null
    private var mStepSensor: Sensor? = null
    private var mLinearAccelerationSensor: Sensor? = null
    private var mAccelerometerSensor: Sensor? = null
    private var mGyroscopeSensor: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAmbientEnabled()

        mContainerView = findViewById(R.id.container) as BoxInsetLayout

        mHearthRate = findViewById(R.id.tv_heart_rate) as TextView
        mStepStatus = findViewById(R.id.tv_step_status) as TextView
        mLinearAcceleration = findViewById(R.id.tv_linear_acceleration) as TextView
        mAcceleration = findViewById(R.id.tv_acceleration) as TextView
        mGyroscope = findViewById(R.id.tv_gyroscope) as TextView

        mStartButton = findViewById(R.id.start_btn) as Button
        mStartButton.setOnClickListener { startMeasure() }

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mSensorManager?.apply {
            mHeartRateSensor = getDefaultSensor(Sensor.TYPE_HEART_RATE)
            mStepSensor = getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mLinearAccelerationSensor = getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            mAccelerometerSensor = getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            mGyroscopeSensor = getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        }
    }

    fun startMeasure() {
        mHearthRate.text = ""
        mStepStatus.text = ""
        mLinearAcceleration.text = ""
        mAcceleration.text = ""

        mSensorManager?.apply {
            var heartSensorRegistered: Boolean = registerListener(this@MainActivity, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)
            var stepSensorRegistered: Boolean = registerListener(this@MainActivity, mStepSensor, SensorManager.SENSOR_DELAY_FASTEST)
            var linearAccelerationRegistered: Boolean = registerListener(this@MainActivity, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            var accelerationRegistered: Boolean = registerListener(this@MainActivity, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
            var gyroscopeRegistered: Boolean = registerListener(this@MainActivity, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)

            if (heartSensorRegistered) {
                mHearthRate.text = "NOT AVAILABLE"
            }
            if (stepSensorRegistered) {
                mStepStatus.text = "NOT AVAILABLE"
            }
            if (linearAccelerationRegistered) {
                mLinearAcceleration.text = "NOT AVAILABLE"
            }
            if (accelerationRegistered) {
                mAcceleration.text = "NOT AVAILABLE"
            }
            if (gyroscopeRegistered) {
                mGyroscope.text = "NOT AVAILABLE"
            }

            if (heartSensorRegistered || stepSensorRegistered || linearAccelerationRegistered || accelerationRegistered || gyroscopeRegistered) {
                measureStarted = true
                mStartButton.text = "Stop"
            } else {
                measureStarted = false
                mStartButton.text = "Start"

                unregisterListener(this@MainActivity)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //TODO:
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            val sensorValue = values[0]

            when (event.sensor?.type) {
                Sensor.TYPE_HEART_RATE -> {
                    val heartRate = Math.round(sensorValue)
                    mHearthRate.text = heartRate.toString()
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    val stepsCount = Math.round(sensorValue)
                    if (stepsCount > 0) {
                        mStepStatus.text = "Yes"
                    } else {
                        mStepStatus.text = "No"
                    }
                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    mLinearAcceleration.text = getLinearAccelerationString(values)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    mGyroscope.text = getLinearAccelerationString(values)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // In this example, alpha is calculated as t / (t + dT),
                    // where t is the low-pass filter's time-constant and
                    // dT is the event delivery rate.

                    var alpha = 0.8f

                    var gravity = FloatArray(3)
                    // Isolate the force of gravity with the low-pass filter.
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                    // Remove the gravity contribution with the high-pass filter.
                    var linearAcceleration = FloatArray(3)
                    linearAcceleration[0] = event.values[0] - gravity[0]
                    linearAcceleration[1] = event.values[1] - gravity[1]
                    linearAcceleration[2] = event.values[2] - gravity[2]
                    mAcceleration.text = getLinearAccelerationString(linearAcceleration)
                }
            }
        }
    }

    fun getLinearAccelerationString(values: FloatArray): String {
        val twoDForm = DecimalFormat("#.#")
        return "x:" + Math.round(values[0]) +
                "\ny:" + Math.round(values[1]) +
                "\nz:" + Math.round(values[2])
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
        super.onExitAmbient()
        updateDisplay()
    }

    private fun updateDisplay() {
        if (isAmbient) {
            mContainerView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
            mHearthRate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            mStepStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            mLinearAcceleration.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            mAcceleration.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            mGyroscope.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            mContainerView.background = null
            mHearthRate.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            mStepStatus.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            mLinearAcceleration.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            mAcceleration.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            mGyroscope.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

}
