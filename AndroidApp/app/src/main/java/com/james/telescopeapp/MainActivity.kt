package com.james.telescopeapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.github.cosinekitty.astronomy.Body
import java.util.Timer
import java.util.TimerTask

private var trackTimer: Timer? = null
private var timerTrackTask: TimerTask? = null

private val DEBUG_TAG = "DEBUG"

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothService: MyServiceInterface

    inner class RepeatListener(direction: Char) : OnTouchListener {

        private var mHandler: Handler? = null
        private val streamRate:Long = 15
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mHandler == null) {
                        mHandler = Handler()
                        mHandler!!.post(actionRunnable)  //assign recursive runnable
                    } else {
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mHandler != null) {
                        mHandler!!.removeCallbacks(actionRunnable)  //remove recursive runnable
                        mHandler = null
                    } else {
                        return true
                    }
                }
            }
            return false
        }

        private var actionRunnable: Runnable = object : Runnable {
            override fun run() {
                bluetoothService.sendManualDirection(direction)
                mHandler?.postDelayed(this, streamRate)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindToBTService()

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener{disconnect()}
        findViewById<Button>(R.id.btnSlew).setOnClickListener{openObjectSelect()}
        findViewById<Button>(R.id.btnCalibrate).setOnClickListener{closeActivity()}

        findViewById<Button>(R.id.btnRight).setOnTouchListener(RepeatListener('r'))
        findViewById<Button>(R.id.btnLeft).setOnTouchListener(RepeatListener('l'))
        findViewById<Button>(R.id.btnUp).setOnTouchListener(RepeatListener('u'))
        findViewById<Button>(R.id.btnDown).setOnTouchListener(RepeatListener('d'))

        trackTimer = Timer()
    }

    private fun startTrack(pBody: Body) {
        timerTrackTask?.cancel()  //cancel currently running task if it exists

        timerTrackTask = object : TimerTask() {
            override fun run() {
                trackBody(pBody)
            }
        }

        trackTimer!!.schedule(timerTrackTask, 0, 10000)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val body = data?.getSerializableExtra("Body") as Body
            val bodyName = data?.getSerializableExtra("BodyName")
            findViewById<Button>(R.id.btnSlew).text = String.format("Tracking: %s", bodyName)
            startTrack(body)
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            timerTrackTask?.cancel()  //Cancel task if assigned
            bluetoothService.sendReset() //Reset scope motors
            findViewById<Button>(R.id.btnSlew).text = getString(R.string.btnSlew)  //Reset text to default
        }
    }

    private fun closeActivity() {
        trackTimer!!.cancel()
        finish()
    }

    private fun openObjectSelect() {
        val intent = Intent(this, ObjectSelectActivity::class.java)
        resultLauncher.launch(intent)
    }


    private fun trackBody(pBody: Body) {
        val hor = SharedTrackingUtility.getHorCoords(pBody)

        if(hor != null) {
            bluetoothService.sendSlewCoords(hor.altitude, hor.azimuth)
        }
    }

    private fun disconnect() {
        //Go back to first screen and end all activities on top of stack
        intent = Intent(this, ConnectActivity::class.java)
        intent.putExtra("disconnecting", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun bindToBTService() {
        //Overriding the serviceConnection so that bluetoothService variable can be set
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bluetoothService = (service as BluetoothService.MyBinder)
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        val btServiceIntent = Intent(this, BluetoothService::class.java)
        bindService(btServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }


}
