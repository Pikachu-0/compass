package com.example.compass

import android.animation.ObjectAnimator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import kotlin.math.roundToInt

class CompassLogic(
    private val sensorManager: SensorManager,
    private val compassView: View,
    private val textView: TextView? = null,
    private val textViewAccuracy: TextView? = null,
    private val constraintLayoutView: ConstraintLayout? = null
) : SensorEventListener {

    private val accelerometerReading = FloatArray(3) { 0f }
    private val magnetometerReading = FloatArray(3) { 0f }
    private val rotationMatrix = FloatArray(9) { 0f }
    private val orientationAngles = FloatArray(3) { 0f }
    var angle = 0 //TODO: make only getter
    private var alreadyExecuted = false

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading[0] = event.values[0]
            accelerometerReading[1] = event.values[1]
            accelerometerReading[2] = event.values[2]
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerReading[0] = event.values[0]
            magnetometerReading[1] = event.values[1]
            magnetometerReading[2] = event.values[2]
            textViewAccuracy?.text = "Accuracy: ${event.accuracy.toString()}/3"
        }

        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        angle = (orientationAngles[0] * 180 / kotlin.math.PI +
                if (orientationAngles[0] >= 0) {
                    0
                } else {
                    360
                }).roundToInt()

        //somehow the sensor return 0 the first time (checked with slow motion camera), also the sensor
        //don't return anything till the view is loaded (setContentView, checked with debugger)
        //only accessible the first time through variable alreadyExecuted
        if (angle != 0 && !alreadyExecuted) {
            compassView.rotation = angle.toFloat() * (-1)
            constraintLayoutView?.isVisible = true
            alreadyExecuted = true
        }
        //smoother movements after first compass orientation (alreadyExecuted = true)
        if (alreadyExecuted) {
            ObjectAnimator.ofFloat(compassView, "rotation", angle.toFloat() * (-1)).apply {
                duration = 500
                start()
            }
        }
        textView?.text = "${angle.toString()}º"

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun activityResume() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun activityPause() {
        sensorManager.unregisterListener(this)
    }

}