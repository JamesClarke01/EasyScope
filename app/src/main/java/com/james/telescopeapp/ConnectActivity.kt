package com.james.telescopeapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class ConnectActivity : AppCompatActivity() {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object { //This data is sent between activities
        val EXTRA_ADDRESS: String = "Device_address"
    }

    private val bluetoothRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth enabling has been cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            listPairedDevices();

        }

        //get bluetooth manager
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        if(bluetoothManager == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        //get bluetooth adapter
        bluetoothAdapter = bluetoothManager!!.adapter
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        //Prompt user to enable bluetooth if not enabled
        if(!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothRequest.launch(enableBtIntent)
        }


    }

    private fun listPairedDevices() {

    }


    private fun openCalibrateActivity() {
        var intent1 = Intent(this, CalibrateActivity::class.java)
        startActivity(intent1);
    }
}
