package com.james.telescopeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.james.telescopeapp.databinding.FragmentStarBinding

class StarFragment : Fragment() {


    private lateinit var activity: Activity
    private lateinit var binding: FragmentStarBinding
    private lateinit var db: StarDBHelper
    private lateinit var starAdapter: StarAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        //Creating View
        binding = FragmentStarBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //After view is created
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity()

        db = StarDBHelper(requireContext())
        starAdapter = StarAdapter(db.getAllStars(), requireContext())

        binding.starRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.starRecyclerView.adapter = starAdapter

        starAdapter.setOnItemClickListener(object : StarAdapter.OnItemClickListener{
            override fun onItemClick(star: Star) {selectStar(star)}
        })
    }

    fun selectStar(star:Star) {
        val resultIntent = Intent()

        //Make the star class serializable if you want more data
        resultIntent.putExtra("name", star.name)
        resultIntent.putExtra("ra", star.ra)
        resultIntent.putExtra("dec", star.dec)

        activity.setResult(Activity.RESULT_OK, resultIntent)
        Toast.makeText(activity, star.name + " selected", Toast.LENGTH_SHORT).show()
        activity.finish()
    }

    override fun onResume() {
        super.onResume()
        starAdapter.refreshData(db.getAllStars())
    }
}