package com.james.telescopeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class CalibrateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibrate)

        findViewById<Button>(R.id.btnCalibrate).setOnClickListener {

            //Calibration Code goes here

            openMainActivity();
        }
    }

    private fun openMainActivity() {
        var intent1 = Intent(this, MainActivity::class.java)
        startActivity(intent1);
    }
}