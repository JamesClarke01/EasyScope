package com.james.telescopeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.james.telescopeapp.databinding.ActivityDbtestBinding
import com.james.telescopeapp.databinding.ActivityDebugBinding
import com.james.telescopeapp.databinding.FragmentStarBinding

class DBTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDbtestBinding
    private lateinit var db: StarDBHelper
    private lateinit var starAdapter: StarAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDbtestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = StarDBHelper(this)
        starAdapter = StarAdapter(db.getAllStars(), this)

        binding.starRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.starRecyclerView.adapter = starAdapter
    }

    override fun onResume() {
        super.onResume()
        starAdapter.refreshData(db.getAllStars())
    }
}