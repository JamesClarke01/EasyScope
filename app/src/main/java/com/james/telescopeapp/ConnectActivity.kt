package com.james.telescopeapp

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

//Bluetooth connectivity adapted from the following tutorial: https://youtu.be/Oz4CBHrxMMs?si=0jpHDqFc4Gat6xS7

class ConnectActivity : AppCompatActivity() {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>


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
            listPairedDevices()
        }

        setupBluetooth()
    }

    private fun setupBluetooth() {
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
        @SuppressLint("MissingPermission") //Bluetooth permissions have already been checked by this point
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                list.add(device)
                Log.i("device", device.toString())
            }
        } else {
            Toast.makeText(this, "No paired bluetooth devices found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        val deviceListView = findViewById<ListView>(R.id.lstDevice)
        deviceListView.adapter = adapter
        deviceListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(list[position])
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        val bluetoothPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.BLUETOOTH
        )
        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )

        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )

        return if (bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
            bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED
        ) {
            // Bluetooth permissions are granted
            true
        } else {
            // Request Bluetooth permissions
            ActivityCompat.requestPermissions(this, permissions, 1)
            false
        }
    }



    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {

        if (checkBluetoothPermissions()) {

            val serviceIntent = Intent(this, BluetoothService::class.java)
            serviceIntent.putExtra("device", device)
            startService(serviceIntent)

            bluetoothAdapter?.cancelDiscovery() //cancel discovery or it will slow connection



            //bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            startConnectActivity()
        }
    }

    private fun startConnectActivity() {
        var mainIntent = Intent(this, CalibrateActivity::class.java)
        startActivity(mainIntent);
    }
}
