package com.james.telescopeapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StarAdapter(private var stars: List<Star>, context: Context): RecyclerView.Adapter<StarAdapter.StarViewHolder>(){

    class StarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtStarName: TextView = itemView.findViewById(R.id.txtStarName)
        val txtStarRa: TextView = itemView.findViewById(R.id.txtStarRa)
        val txtStarDec: TextView = itemView.findViewById(R.id.txtStarDec)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.star_item, parent, false)
        return StarViewHolder(view)
    }

    override fun getItemCount(): Int = stars.size

    override fun onBindViewHolder(holder: StarViewHolder, position: Int) {
        val star = stars[position]
        holder.txtStarName.text = star.name
        holder.txtStarRa.text = star.ra.toString()
        holder.txtStarDec.text = star.dec.toString()
    }

    fun refreshData(newStars: List<Star>){
        stars = newStars
        notifyDataSetChanged()
    }

}