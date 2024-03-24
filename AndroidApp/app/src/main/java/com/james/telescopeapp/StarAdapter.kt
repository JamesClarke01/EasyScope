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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.star_item, parent, false)
        return StarViewHolder(view)
    }

    override fun onBindViewHolder(holder: StarViewHolder, position: Int) {
        val star = stars[position]
        holder.txtStarName.text = star.name

        holder.itemView.setOnClickListener{
            onItemClick(star)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(star:Star)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    private fun onItemClick(star: Star) {
        onItemClickListener?.onItemClick(star)
    }

    override fun getItemCount(): Int = stars.size

    fun refreshData(newStars: List<Star>){
        stars = newStars
        notifyDataSetChanged()
    }

}