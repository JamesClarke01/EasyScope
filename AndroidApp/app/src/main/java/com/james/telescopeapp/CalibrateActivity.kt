package com.james.telescopeapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast

class CalibrateActivity : AppCompatActivity(), SensorEventListener {

    private var bluetoothService: MyServiceInterface? = null

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val accelerometerValues = FloatArray(3)
    private val magnetometerValues = FloatArray(3)

    private var azimuth: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibrate)

        bindToBTService()

        setupSensors()


        findViewById<Button>(R.id.btnCalibrate).setOnClickListener {calibrate()}
        findViewById<Button>(R.id.btnToMain).setOnClickListener {openMainActivity()}
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private fun calibrate() {
        Log.d("SensorValues", "Accelerometer: ${accelerometerValues.joinToString()}")
        Log.d("SensorValues", "Magnetometer: ${magnetometerValues.joinToString()}")


        Toast.makeText(this, azimuth.toString(), Toast.LENGTH_SHORT).show()
    }


    private fun bindToBTService() {
        //Overriding the serviceConnection so that bluetoothService variable can be set
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bluetoothService = (service as BluetoothService.MyBinder).also {
                    // Service is connected, you can now call methods on the service
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bluetoothService = null
            }
        }
        val btServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(btServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun openMainActivity() {
        var mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent);
    }

    override fun onSensorChanged(event: SensorEvent?) {

        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> event.values.copyInto(accelerometerValues)
            Sensor.TYPE_MAGNETIC_FIELD -> event.values.copyInto(magnetometerValues)
        }

        // Calculate the azimuth using accelerometerValues and magnetometerValues.
        val rotationMatrix = FloatArray(9)
        val orientationValues = FloatArray(3)

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerValues)
        SensorManager.getOrientation(rotationMatrix, orientationValues)

        azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        
    }
}