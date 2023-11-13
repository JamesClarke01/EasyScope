package com.james.telescopeapp

import android.R.attr.button
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.UUID


private val TAG = "Main"



class MainActivity : AppCompatActivity() {

    var btSocket: BluetoothSocket? = null;

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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
                    mHandler?.postDelayed(this, 500)
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
                    mHandler?.postDelayed(this, 500)
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
                    mHandler?.postDelayed(this, 500)
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
                    mHandler?.postDelayed(this, 500)
                }
            }
        })



    }
}