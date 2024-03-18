package com.james.telescopeapp

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.provider.Settings.Global
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

interface MyServiceInterface {
    fun write(data: String)

    fun sendSlewCoords(altitude: Double, azimuth: Double)

    fun sendCalibrationData(azimuth:Double)

    fun sendReset()

    fun sendManUp()
    fun sendManLeft()
    fun sendManDown()
    fun sendManRight()

    fun sendTweakUp()
    fun sendTweakLeft()
    fun sendTweakDown()
    fun sendTweakRight()
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

        override fun sendSlewCoords(altitude: Double, azimuth: Double) {
            val dataJson = JSONObject()
            dataJson.put("Altitude", altitude)
            dataJson.put("Azimuth", azimuth)

            val instructionJson = JSONObject()
            instructionJson.put("Instruction", "Slew")
            instructionJson.put("Data", dataJson)

            write(instructionJson.toString())
        }

        override fun sendManUp() {
            write("w")
        }
        override fun sendManLeft() {
            write("a")
        }
        override fun sendManDown() {
            write("s")
        }
        override fun sendManRight() {
            write("d")
        }

        override fun sendTweakUp() {
            write("i")
        }
        override fun sendTweakLeft() {
            write("j")
        }
        override fun sendTweakDown() {
            write("k")
        }
        override fun sendTweakRight() {
            write("l")
        }

        override fun sendCalibrationData(azimuth: Double) {
            val dataJson = JSONObject()
            dataJson.put("Azimuth", azimuth)

            val instructionJson = JSONObject()
            instructionJson.put("Instruction", "Calibrate")
            instructionJson.put("Data", dataJson)

            write(instructionJson.toString())
        }

        override fun sendReset() {
            val instructionJson = JSONObject()
            instructionJson.put("Instruction", "Reset")
            write(instructionJson.toString())
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