package com.james.telescopeapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class ObjectSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_select)
        setupTabView()
        findViewById<Button>(R.id.btnCancel).setOnClickListener{cancelSlew()}
    }

    private fun cancelSlew() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun setupTabView() {
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val fragmentAdapter = FragmentAdapter(supportFragmentManager)

        fragmentAdapter.addFragment(StarFragment(), "Stars")
        fragmentAdapter.addFragment(PlanetFragment(), "Planets")

        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)
    }
}