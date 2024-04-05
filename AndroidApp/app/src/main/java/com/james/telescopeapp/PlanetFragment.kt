package com.james.telescopeapp

import android.app.Activity
import android.content.Intent
import android.media.Image.Plane
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.james.telescopeapp.databinding.FragmentPlanetBinding
import io.github.cosinekitty.astronomy.Body

class PlanetFragment : Fragment() {

    private lateinit var activity: Activity
    private lateinit var binding: FragmentPlanetBinding
    private lateinit var planetAdapter: PlanetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlanetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val planets = listOf(Planet(Body.Moon, "moon"),
                             Planet(Body.Jupiter, "jupiter"),
                             Planet(Body.Saturn, "saturn"),
                             Planet(Body.Venus, "venus"),
                             Planet(Body.Mars, "mars"),
                             Planet(Body.Neptune, "neptune"),
                             Planet(Body.Uranus, "uranus"),
                             Planet(Body.Mercury, "mercury"))

        activity = requireActivity()

        planetAdapter = PlanetAdapter(requireContext(), planets)

        binding.planetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.planetRecyclerView.adapter = planetAdapter

        planetAdapter.setOnItemClickListener(object: PlanetAdapter.OnItemClickListener {
            override fun onItemClick(planet: Planet) {selectPlanet(planet)}
        })
    }

    private fun selectPlanet(planet:Planet) {
        if (SharedTrackingUtility.bodyUnderHorizon(planet.body)) {
            Toast.makeText(requireActivity(), "Not Visible", Toast.LENGTH_SHORT).show()
        } else {
            val resultIntent = Intent()
            val activity = requireActivity()
            Toast.makeText(
                requireActivity(),
                String.format("Tracking %s", planet.body.name),
                Toast.LENGTH_SHORT
            ).show()
            resultIntent.putExtra("Body", planet.body)
            resultIntent.putExtra("BodyName", planet.body.name)
            activity.setResult(Activity.RESULT_OK, resultIntent)
            activity.finish()
        }
    }


}