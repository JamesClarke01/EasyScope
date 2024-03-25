package com.james.telescopeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.cosinekitty.astronomy.Body

class PlanetAdapter(private val planets: List<Body>) : RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder>(){

    private var onItemClickListener: PlanetAdapter.OnItemClickListener? = null

    class PlanetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtPlanetName: TextView = itemView.findViewById(R.id.txtStarName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.star_item, parent, false)
        return PlanetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanetViewHolder, position: Int) {
        val planet = planets[position]

        holder.txtPlanetName.text = planet.name

        holder.itemView.setOnClickListener{
            onItemClick(planet)
        }
    }

    override fun getItemCount(): Int = planets.size

    interface OnItemClickListener {
        fun onItemClick(planet:Body)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    private fun onItemClick(planet:Body) {
        onItemClickListener?.onItemClick(planet)
    }
}