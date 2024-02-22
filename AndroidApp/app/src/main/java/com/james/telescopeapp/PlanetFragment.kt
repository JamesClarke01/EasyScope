package com.james.telescopeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import io.github.cosinekitty.astronomy.Body

class PlanetFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnMoon).setOnClickListener{trackPlanet(Body.Moon)}
        view.findViewById<Button>(R.id.btnJupiter).setOnClickListener{trackPlanet(Body.Jupiter)}
        view.findViewById<Button>(R.id.btnSaturn).setOnClickListener{trackPlanet(Body.Saturn)}
        view.findViewById<Button>(R.id.btnVenus).setOnClickListener{trackPlanet(Body.Venus)}
        view.findViewById<Button>(R.id.btnMars).setOnClickListener{trackPlanet(Body.Mars)}
        view.findViewById<Button>(R.id.btnNeptune).setOnClickListener{trackPlanet(Body.Neptune)}
        view.findViewById<Button>(R.id.btnUranus).setOnClickListener{trackPlanet(Body.Uranus)}
        view.findViewById<Button>(R.id.btnMercury).setOnClickListener{trackPlanet(Body.Mercury)}
    }

    private fun trackPlanet(body:Body) {
        val resultIntent = Intent()
        val activity = requireActivity()
        Toast.makeText(requireActivity(), "Tracking $...".format(body.name), Toast.LENGTH_SHORT).show()
        resultIntent.putExtra("Body", body)
        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()
    }
}