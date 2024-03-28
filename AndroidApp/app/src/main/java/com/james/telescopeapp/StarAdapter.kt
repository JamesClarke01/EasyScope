package com.james.telescopeapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.defineStar

class StarAdapter( private val context: Context, private var stars: List<Star>): RecyclerView.Adapter<StarAdapter.StarViewHolder>(){

    private var onItemClickListener: OnItemClickListener? = null

    class StarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtStarName: TextView = itemView.findViewById(R.id.txtStarName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.star_item, parent, false)
        return StarViewHolder(view)
    }

    override fun onBindViewHolder(holder: StarViewHolder, position: Int) {
        val star = stars[position]

        holder.txtStarName.text = star.name

        if(SharedTrackingUtility.starUnderHorizon(star)) {
            holder.txtStarName.setTextColor(Color.GRAY)
        } else {
            holder.txtStarName.setTextColor(ContextCompat.getColor(context, R.color.foreground))
        }

        holder.itemView.setOnClickListener{
            onItemClick(star)
        }
    }

    override fun getItemCount(): Int = stars.size

    interface OnItemClickListener {
        fun onItemClick(star:Star)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    private fun onItemClick(star: Star) {
        onItemClickListener?.onItemClick(star)
    }

    fun refreshData(newStars: List<Star>){
        stars = newStars
        notifyDataSetChanged()
    }

}