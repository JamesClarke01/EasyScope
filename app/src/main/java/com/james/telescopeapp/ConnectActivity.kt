package com.james.telescopeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class ConnectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {

            /*
            val myToast = Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT)
            myToast.show()

            bluetoothConnect()

             */
            openCalibrateActivity();
        }
    }
    private fun openCalibrateActivity() {
        var intent1 = Intent(this, CalibrateActivity::class.java)
        startActivity(intent1);
    }
}
