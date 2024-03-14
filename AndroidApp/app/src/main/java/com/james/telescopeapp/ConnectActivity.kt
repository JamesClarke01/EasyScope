package com.james.telescopeapp

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Bluetooth connectivity adapted from the following tutorial: https://youtu.be/Oz4CBHrxMMs?si=0jpHDqFc4Gat6xS7

class ConnectActivity : AppCompatActivity() {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

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

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {listPairedDevices()}

        requestBluetoothPermissions()
        setupBluetooth()
        listPairedDevices()
    }

    override fun onResume() {
        super.onResume()

        //stop bluetooth service
        val serviceIntent = Intent(this, BluetoothService::class.java)
        stopService(serviceIntent)

        setupBluetooth()
        listPairedDevices()
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

    private fun requestBluetoothPermissions() {
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

        if (bluetoothPermission != PackageManager.PERMISSION_GRANTED &&
            bluetoothAdminPermission != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    private fun bluetoothEnabled(): Boolean {
        if(bluetoothAdapter != null) {
            if(bluetoothAdapter!!.isEnabled) {
                return true
            }
        }
        return false
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

        @SuppressLint("MissingPermission")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list.map{it.name})
        val deviceListView = findViewById<ListView>(R.id.lstDevice)
        deviceListView.adapter = adapter
        deviceListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(list[position])
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {

        if (bluetoothEnabled()) {

            val serviceIntent = Intent(this, BluetoothService::class.java)
            serviceIntent.putExtra("device", device)
            startService(serviceIntent)

            bluetoothAdapter?.cancelDiscovery() //cancel discovery or it will slow connection

            startConnectActivity()
        } else {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show()
            setupBluetooth()
        }

    }

    private fun startConnectActivity() {
        val intent = Intent(this, CalibrateActivity::class.java)
        startActivity(intent)
    }
}
