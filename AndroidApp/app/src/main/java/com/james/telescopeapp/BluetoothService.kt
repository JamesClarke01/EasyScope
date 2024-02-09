package com.james.telescopeapp

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.io.IOException
import java.util.UUID

interface MyServiceInterface {
    fun write(data: String)
}

class BluetoothService : Service() {

    private var bluetoothSocket: BluetoothSocket? = null
    private val binder = MyBinder()

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val device = intent?.getParcelableExtra<BluetoothDevice>("device")
        if(device != null) {
            try {
                if (bluetoothSocket == null) {
                    val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)

                    bluetoothSocket?.connect()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    inner class MyBinder: Binder(), MyServiceInterface {
        override fun write(data: String) {
            if (bluetoothSocket != null) {
                try { // Converting the string to bytes for transferring
                    bluetoothSocket!!.outputStream.write(data.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothSocket!!.close()
        bluetoothSocket = null
    }
}