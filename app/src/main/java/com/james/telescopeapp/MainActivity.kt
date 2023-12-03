package com.james.telescopeapp

import android.Manifest
import android.R.attr.button
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
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
import java.io.IOException
import java.util.UUID
import java.util.Calendar


private val TAG = "DebugTag"

private val REQUEST_LOCATION_PERMISSION = 0


class MainActivity : AppCompatActivity() {

    var btSocket: BluetoothSocket? = null;
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient;

    fun bluetoothConnect() {
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        //Get bluetooth adapter
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        //Will have to check if bluetooth is enabled here and ask user

        //Connect to first device in paired devices (will have to add logic to select scope in future)

        try {
            if (btSocket == null) {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                val hc05 =
                    bluetoothAdapter?.getRemoteDevice(pairedDevices?.firstOrNull().toString())

                btSocket = hc05?.createInsecureRfcommSocketToServiceRecord(myUUID)
                Log.v(TAG, "Created socket")

                bluetoothAdapter?.cancelDiscovery() //cancel discovery or it will slow connection
                Log.v(TAG, "Cancelled Discovery")

                btSocket?.connect();
                Log.v(TAG, "Connected to socket")
            }
        } catch (e: IOException ) {
            e.printStackTrace()
        }
    }

    fun writeToBT(data:String) {
        if (btSocket != null) {
            try { // Converting the string to bytes for transferring
                btSocket!!.outputStream.write(data.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkLocationPermissions():Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
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
                Toast.makeText(this, location.longitude.toString() + " " + location.latitude.toString(), Toast.LENGTH_SHORT).show()
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val STREAM_RATE:Long = 15;
        setContentView(R.layout.activity_main)



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            val myToast = Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT)
            myToast.show()

            bluetoothConnect()
        }

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            val myToast = Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT)
            if (btSocket != null) {
                btSocket?.close()
                btSocket = null
            }
        }

        findViewById<Button>(R.id.btnMove).setOnClickListener {
            val altitude = findViewById<EditText>(R.id.edtAltitude).text;
            val azimuth = findViewById<EditText>(R.id.edtAzimuth).text;

            writeToBT("(" + altitude + ',' + azimuth + ')');
        }

        findViewById<Button>(R.id.btnPolaris).setOnClickListener {

            //Get Time
            val currTime = Calendar.getInstance();

            val time = Time(currTime.get(Calendar.YEAR),currTime.get(Calendar.MONTH),
                currTime.get(Calendar.DATE),currTime.get(Calendar.HOUR_OF_DAY),
                currTime.get(Calendar.MINUTE), currTime.get(Calendar.SECOND).toDouble());





            //val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            /*
            val observer = Observer(longitude, lattitude, 0.0)


            defineStar(Body.Star1, 3.06, 89.36572, 1000.0)

            val equ_ofdate: Equatorial = equator(Body.Star1, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)

            val hor: Topocentric = horizon(time, observer, equ_ofdate.ra, equ_ofdate.dec, Refraction.Normal)

            Log.d("TAG", ("%8.2f %8.2f".format(hor.azimuth, hor.altitude)));
            */

        }



        //https://stackoverflow.com/questions/10511423/android-repeat-action-on-pressing-and-holding-a-button
        findViewById<Button>(R.id.btnRight).setOnTouchListener(object: OnTouchListener {
            private var mHandler: Handler? = null

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler == null) {
                            mHandler = Handler()
                            mHandler!!.post(mAction)  //assign recursive runnable
                        } else {
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mHandler != null) {
                            mHandler!!.removeCallbacks(mAction)  //remove recursive runnable
                            mHandler = null
                        } else {
                            return true
                        }
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    writeToBT("r")
                    mHandler?.postDelayed(this, STREAM_RATE)
                }
            }
        })

        findViewById<Button>(R.id.btnLeft).setOnTouchListener(object: OnTouchListener {
            private var mHandler: Handler? = null

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler == null) {
                            mHandler = Handler()
                            mHandler!!.post(mAction)  //assign recursive runnable
                        } else {
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mHandler != null) {
                            mHandler!!.removeCallbacks(mAction)  //remove recursive runnable
                            mHandler = null
                        } else {
                            return true
                        }
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    writeToBT("l")
                    mHandler?.postDelayed(this, STREAM_RATE)
                }
            }
        })

        findViewById<Button>(R.id.btnUp).setOnTouchListener(object: OnTouchListener {
            private var mHandler: Handler? = null

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler == null) {
                            mHandler = Handler()
                            mHandler!!.post(mAction)  //assign recursive runnable
                        } else {
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mHandler != null) {
                            mHandler!!.removeCallbacks(mAction)  //remove recursive runnable
                            mHandler = null
                        } else {
                            return true
                        }
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    writeToBT("u")
                    mHandler?.postDelayed(this, STREAM_RATE)
                }
            }
        })

        findViewById<Button>(R.id.btnDown).setOnTouchListener(object: OnTouchListener {
            private var mHandler: Handler? = null

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler == null) {
                            mHandler = Handler()
                            mHandler!!.post(mAction)  //assign recursive runnable
                        } else {
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mHandler != null) {
                            mHandler!!.removeCallbacks(mAction)  //remove recursive runnable
                            mHandler = null
                        } else {
                            return true
                        }
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    writeToBT("d")
                    mHandler?.postDelayed(this, STREAM_RATE)
                }
            }
        })



    }
}