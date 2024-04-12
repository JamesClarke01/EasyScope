package com.james.telescopeapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.cosinekitty.astronomy.Body

class PlanetAdapter(private val context: Context, private val planets: List<Planet>) : RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder>(){

    private var onItemClickListener: PlanetAdapter.OnItemClickListener? = null

    class PlanetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtPlanetName: TextView = itemView.findViewById(R.id.txtStarName)
        val planetImage: ImageView = itemView.findViewById(R.id.objectImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.star_item, parent, false)
        return PlanetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanetViewHolder, position: Int) {
        val planet = planets[position]

        holder.txtPlanetName.text = planet.body.name

        val resourceId = context.resources.getIdentifier(planet.imageName, "drawable",context.packageName)
        val drawable = AppCompatResources.getDrawable(context, resourceId)

        holder.planetImage.setImageDrawable(drawable)


        if(SharedTrackingUtility.bodyUnderHorizon(planet.body)) {
            holder.txtPlanetName.setTextColor(Color.GRAY)
        } else {
            holder.txtPlanetName.setTextColor(ContextCompat.getColor(context, R.color.foreground))
        }

        holder.itemView.setOnClickListener{
            onItemClick(planet)
        }
    }

    override fun getItemCount(): Int = planets.size

    interface OnItemClickListener {
        fun onItemClick(planet:Planet)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    private fun onItemClick(planet:Planet) {
        onItemClickListener?.onItemClick(planet)
    }
}