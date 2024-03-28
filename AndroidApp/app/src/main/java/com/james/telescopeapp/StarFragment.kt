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
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.defineStar

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
        starAdapter = StarAdapter(requireContext(), db.getAllStars())

        binding.starRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.starRecyclerView.adapter = starAdapter

        starAdapter.setOnItemClickListener(object : StarAdapter.OnItemClickListener{
            override fun onItemClick(star: Star) {selectStar(star)}
        })
    }

    fun selectStar(star:Star) {
        if(SharedTrackingUtility.starUnderHorizon(star)) {
            Toast.makeText(requireActivity(), "Not Visible", Toast.LENGTH_SHORT).show()
        } else {
            val resultIntent = Intent()

            defineStar(Body.Star1, star.ra, star.dec, 1000.0)  //define star (object in space)

            resultIntent.putExtra("Body", Body.Star1)
            resultIntent.putExtra("BodyName", star.name)

            activity.setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(activity, star.name + " selected", Toast.LENGTH_SHORT).show()
            activity.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        starAdapter.refreshData(db.getAllStars())
    }
}