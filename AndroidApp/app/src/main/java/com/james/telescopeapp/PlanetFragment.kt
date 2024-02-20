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

        view.findViewById<Button>(R.id.btnMoon).setOnClickListener{moon()}
    }

    private fun moon() {
        val resultIntent = Intent()
        val activity = requireActivity()
        Toast.makeText(requireActivity(), "Tracking Moon...", Toast.LENGTH_SHORT).show()
        resultIntent.putExtra("Body", Body.Moon)

        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()

    }
}