package com.james.telescopeapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Equatorial
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.Topocentric
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

private const val REQUEST_LOCATION_PERMISSION = 0

private var lattitude = 0.0
private var longitude = 0.0

private var trackTimer: Timer? = null
private var timerTrackTask: TimerTask? = null

private val DEBUG_TAG = "DEBUG"

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothService: MyServiceInterface
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    inner class RepeatListener(direction: Direction) : OnTouchListener {

        private var mHandler: Handler? = null
        private val streamRate:Long = 15
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mHandler == null) {
                        mHandler = Handler()
                        mHandler!!.post(actionRunnable)  //assign recursive runnable
                    } else {
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mHandler != null) {
                        mHandler!!.removeCallbacks(actionRunnable)  //remove recursive runnable
                        mHandler = null
                    } else {
                        return true
                    }
                }
            }
            return false
        }

        private var actionRunnable: Runnable = object : Runnable {
            override fun run() {

                if (timerTrackTask == null) { //If not tracking, buttons manually move scope
                    when (direction) {
                        Direction.UP -> bluetoothService.sendManUp()
                        Direction.LEFT -> bluetoothService.sendManLeft()
                        Direction.DOWN -> bluetoothService.sendManDown()
                        Direction.RIGHT -> bluetoothService.sendManRight()
                    }
                } else { //if tracking, buttons tweak scope
                    when (direction) {
                        Direction.UP -> bluetoothService.sendTweakUp()
                        Direction.LEFT -> bluetoothService.sendTweakLeft()
                        Direction.DOWN -> bluetoothService.sendTweakDown()
                        Direction.RIGHT -> bluetoothService.sendTweakRight()
                    }
                }

                mHandler?.postDelayed(this, streamRate)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindToBTService()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener{disconnect()}
        findViewById<Button>(R.id.btnDebug).setOnClickListener{openDebugActivity()}
        findViewById<Button>(R.id.btnSlew).setOnClickListener{openObjectSelect()}
        findViewById<Button>(R.id.btnCalibrate).setOnClickListener{closeActivity()}

        findViewById<Button>(R.id.btnUp).setOnTouchListener(RepeatListener(Direction.UP))
        findViewById<Button>(R.id.btnLeft).setOnTouchListener(RepeatListener(Direction.LEFT))
        findViewById<Button>(R.id.btnDown).setOnTouchListener(RepeatListener(Direction.DOWN))
        findViewById<Button>(R.id.btnRight).setOnTouchListener(RepeatListener(Direction.RIGHT))

        trackTimer = Timer()
    }

    private fun openDBTest() {
        val intent = Intent(this, DBTestActivity::class.java)
        startActivity(intent)
    }

    private fun startTrack(pBody: Body) {
        timerTrackTask?.cancel()  //cancel currently running task if it exists

        timerTrackTask = object : TimerTask() {
            override fun run() {
                trackBody(pBody)
            }
        }

        trackTimer!!.schedule(timerTrackTask, 0, 10000)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val body = data?.getSerializableExtra("Body") as Body
            val bodyName = data?.getSerializableExtra("BodyName")
            findViewById<Button>(R.id.btnSlew).text = String.format("Tracking: %s", bodyName)
            startTrack(body)
        }
    }

    private fun closeActivity() {
        trackTimer!!.cancel()
        finish()
    }

    private fun openObjectSelect() {
        val intent = Intent(this, ObjectSelectActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun trackBody(pBody: Body) {
        //Get Time
        val currTime = Calendar.getInstance()

        val time = Time(currTime.get(Calendar.YEAR),currTime.get(Calendar.MONTH),
            currTime.get(Calendar.DATE),currTime.get(Calendar.HOUR_OF_DAY),
            currTime.get(Calendar.MINUTE), currTime.get(Calendar.SECOND).toDouble())

        //Get Location
        getCurrentLocation()

        val observer = Observer(lattitude, longitude, 0.0)  //define observer (scope position on Earth)

        val equ_ofdate: Equatorial = equator(pBody!!, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)  //define equatorial coordinates of star for current time

        val hor: Topocentric = horizon(time, observer, equ_ofdate.ra, equ_ofdate.dec, Refraction.Normal)  //translate equatorial coordinates to horizontal coordinates

        Log.d(DEBUG_TAG, String.format("Altitude: %f, Azimuth: %f", hor.altitude, hor.azimuth))

        bluetoothService.sendSlewCoords(hor.altitude, hor.azimuth)
    }

    private fun openDebugActivity() {
        val intent = Intent(this, DebugActivity::class.java)
        startActivity(intent)
    }

    private fun disconnect() {
        //Go back to first screen and end all activities on top of stack
        intent = Intent(this, ConnectActivity::class.java)
        intent.putExtra("disconnecting", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun checkLocationPermissions():Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
            val location:Location?=task.result
            if(location==null) {
                Toast.makeText(this, "Null Received", Toast.LENGTH_SHORT).show()
            } else {
                lattitude = location.latitude
                longitude = location.longitude
            }
        }
    }

    private fun locationEnabled():Boolean {
        val locationManager:LocationManager=getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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

    private fun bindToBTService() {
        //Overriding the serviceConnection so that bluetoothService variable can be set
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bluetoothService = (service as BluetoothService.MyBinder)
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        val btServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(btServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }


}
