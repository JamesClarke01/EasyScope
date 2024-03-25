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
        val planets = listOf(Body.Moon, Body.Jupiter, Body.Saturn, Body.Venus, Body.Mars,
            Body.Neptune, Body.Uranus, Body.Mercury)

        activity = requireActivity()

        planetAdapter = PlanetAdapter(planets)

        binding.planetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.planetRecyclerView.adapter = planetAdapter

        planetAdapter.setOnItemClickListener(object: PlanetAdapter.OnItemClickListener {
            override fun onItemClick(planet: Body) {selectPlanet(planet)}
        })
    }

    private fun selectPlanet(body:Body) {
        val resultIntent = Intent()
        val activity = requireActivity()
        Toast.makeText(requireActivity(), String.format("Tracking %s", body.name), Toast.LENGTH_SHORT).show()
        resultIntent.putExtra("Body", body)
        resultIntent.putExtra("BodyName", body.name)
        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()
    }
}