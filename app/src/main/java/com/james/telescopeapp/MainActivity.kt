package com.james.telescopeapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.UUID


private val TAG = "Main"



class MainActivity : AppCompatActivity() {

    var btSocket: BluetoothSocket? = null;

    fun btnConnectOnClick() {
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        val myToast = Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT)
        myToast.show()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            btnConnectOnClick()
        }

        findViewById<Button>(R.id.btnNorth).setOnClickListener {
            val myToast = Toast.makeText(this, "North!", Toast.LENGTH_SHORT)
            myToast.show()

            if (btSocket != null) {
                try { // Converting the string to bytes for transferring
                    btSocket!!.outputStream.write("n".toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        findViewById<Button>(R.id.btnSouth).setOnClickListener {
            val myToast = Toast.makeText(this, "South!", Toast.LENGTH_SHORT)
            myToast.show()

            if (btSocket != null) {
                try { // Converting the string to bytes for transferring
                    btSocket!!.outputStream.write("s".toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        Log.v(TAG, "Starting")



    }
}