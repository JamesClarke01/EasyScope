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
import io.github.cosinekitty.astronomy.defineStar
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import org.json.JSONObject
import java.util.Calendar

private const val REQUEST_LOCATION_PERMISSION = 0

private var lattitude = 0.0
private var longitude = 0.0

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothService: MyServiceInterface
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    inner class RepeatListener(direction: Char) : OnTouchListener {
        //Adapted from https://stackoverflow.com/questions/10511423/android-repeat-action-on-pressing-and-holding-a-button
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
                bluetoothService.sendManualDirection(direction)
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

        findViewById<Button>(R.id.btnRight).setOnTouchListener(RepeatListener('r'))
        findViewById<Button>(R.id.btnLeft).setOnTouchListener(RepeatListener('l'))
        findViewById<Button>(R.id.btnUp).setOnTouchListener(RepeatListener('u'))
        findViewById<Button>(R.id.btnDown).setOnTouchListener(RepeatListener('d'))
    }

    private fun openDBTest() {
        val intent = Intent(this, DBTestActivity::class.java)
        startActivity(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val body = data?.getSerializableExtra("Body") as Body
            trackBody(body)
        }
    }

    private fun openObjectSelect() {
        val intent = Intent(this, ObjectSelectActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun trackBody(pBody:Body) {
        //findViewById<Button>(R.id.btnSlew).text = getString(R.string.btnTracking, name)

        //Get Time
        val currTime = Calendar.getInstance()

        val time = Time(currTime.get(Calendar.YEAR),currTime.get(Calendar.MONTH),
            currTime.get(Calendar.DATE),currTime.get(Calendar.HOUR_OF_DAY),
            currTime.get(Calendar.MINUTE), currTime.get(Calendar.SECOND).toDouble())

        //Get Location
        getCurrentLocation()

        val observer = Observer(lattitude, longitude, 0.0)  //define observer (scope position on Earth)

        //defineStar(Body.Star1, ra, dec, 1000.0)  //define star (object in space)

        val equ_ofdate: Equatorial = equator(pBody, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)  //define equatorial coordinates of star for current time

        val hor: Topocentric = horizon(time, observer, equ_ofdate.ra, equ_ofdate.dec, Refraction.Normal)  //translate equatorial coordinates to horizontal coordinates

        Toast.makeText(this, hor.azimuth.toString() + ' ' + hor.altitude, Toast.LENGTH_SHORT).show()

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
                //Toast.makeText(this, location.longitude.toString() + " " + location.latitude.toString(), Toast.LENGTH_SHORT).show()
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
