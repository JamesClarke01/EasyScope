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
        val tabTitles = arrayOf("Bleep", "Bloop")
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val fragmentAdapter = FragmentAdapter(supportFragmentManager)

        /*
        for (i in tabTitles.indices) {
            val tabItem = tabLayout.newTab()
            val customView = layoutInflater.inflate(R.layout.tab_item, null)
            val textView = customView.findViewById<TextView>(R.id.tab_text)
            textView.text = tabTitles[i]
            tabItem.customView = customView
            tabLayout.addTab(tabItem)
        }*/

        fragmentAdapter.addFragment(StarFragment(), "Stars")
        fragmentAdapter.addFragment(PlanetFragment(), "Planets")

        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)
    }
}