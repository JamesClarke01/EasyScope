package com.james.telescopeapp

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task

private const val REQUEST_LOCATION_PERMISSION = 0

class CalibrateActivity : AppCompatActivity(), SensorEventListener {

    private var bluetoothService: MyServiceInterface? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val accelerometerValues = FloatArray(3)
    private val magnetometerValues = FloatArray(3)

    private var azimuth: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibrate)

        lockNextButton()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        bindToBTService()

        setupSensors()

        findViewById<Button>(R.id.btnCalibrate).setOnClickListener{calibrate()}
        findViewById<Button>(R.id.btnToMain).setOnClickListener{openMainActivity()}
    }

    override fun onResume() {
        super.onResume()
        bluetoothService?.sendReset()
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
        Toast.makeText(this, String.format("Direction: %f", azimuth), Toast.LENGTH_SHORT).show()
        lockNextButton()
        bluetoothService?.sendCalibrationData(azimuth)
        getCurrentLocation()
    }

    private fun lockNextButton() {
        var nextButton =  findViewById<Button>(R.id.btnToMain)

        nextButton.setBackgroundResource(R.drawable.button_greyed)
        nextButton.setTextColor(getColor(R.color.foreground_greyed))
        nextButton.setOnClickListener{null}
    }

    private fun unlockNextButton() {
        var nextButton =  findViewById<Button>(R.id.btnToMain)

        nextButton.setOnClickListener{openMainActivity()}
        nextButton.setBackgroundResource(R.drawable.button_background)
        nextButton.setTextColor(getColor(R.color.foreground))
    }

    private fun bindToBTService() {
        //Overriding the serviceConnection so that bluetoothService variable can be set
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bluetoothService = (service as BluetoothService.MyBinder)

                bluetoothService?.sendReset()
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

        azimuth = (Math.toDegrees(orientationValues[0].toDouble()) + 360) % 360  //returns value in range 0-360
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds (e.g., every 10 seconds)
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Request high accuracy location updates
    }

    private fun getCurrentLocation() {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            requestLocationPermissions()
            return
        }

        // Check if location settings are enabled
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, 1001)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            } else {
                Toast.makeText(this, "Failed to check location settings", Toast.LENGTH_SHORT).show()
            }
        }

        task.addOnSuccessListener {
            getLocation()
        }
    }

    private fun getLocation() {
        Toast.makeText(
            this,
            String.format("Retrieving Location..."),
            Toast.LENGTH_SHORT
        ).show()

        // Request location updates using FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                getLocation()
            } else {
                Toast.makeText(this, "Location services not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Location callback to handle location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            val location = locationResult.lastLocation
            // Location found, handle the location data
            if(location != null) {
                handleLocation(location!!)
            } else {
                Toast.makeText(this@CalibrateActivity, "Enable location.", Toast.LENGTH_SHORT).show()
            }
            // Stop receiving location updates
            stopLocationUpdates()
        }
    }

    // Function to handle the location data
    private fun handleLocation(location: Location) {
        SharedTrackingUtility.latitude = location.latitude
        SharedTrackingUtility.longitude = location.longitude
        // Do something with the location data (e.g., display it)
        Toast.makeText(
            this,
            String.format("Latitude: %f, Longitude: %f", SharedTrackingUtility.latitude, SharedTrackingUtility.longitude),
            Toast.LENGTH_SHORT
        ).show()

        unlockNextButton()
    }

    // Function to stop receiving location updates
    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }
    }
}