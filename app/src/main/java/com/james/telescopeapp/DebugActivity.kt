package com.james.telescopeapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.EditText

class DebugActivity : AppCompatActivity() {

    lateinit var bluetoothService: MyServiceInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        bindToBTService()

        findViewById<Button>(R.id.btnMove).setOnClickListener{moveScope()}
    }

    private fun moveScope() {
        val altitude = findViewById<EditText>(R.id.edtAltitude).text
        val azimuth = findViewById<EditText>(R.id.edtAzimuth).text

        bluetoothService.write("($altitude,$azimuth)")
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
                //bluetoothService = null
            }
        }
        val btServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(btServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}