package com.james.telescopeapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button

class CalibrateActivity : AppCompatActivity() {

    private var bluetoothService: MyServiceInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibrate)

        bindToBTService()

        findViewById<Button>(R.id.btnCalibrate).setOnClickListener {

            //Calibration Code goes here

            openMainActivity();
        }
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
}