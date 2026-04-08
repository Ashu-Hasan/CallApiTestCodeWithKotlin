package com.ashu.callapitestcode.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashu.callapitestcode.R
import com.ashu.callapitestcode.data.model.ForecastItem
import com.ashu.callapitestcode.other.graphs.ImageLoaderUtil

class ForecastAdapter(
    private val context: Context,
    private val list: List<ForecastItem>
) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvDay.text = item.day
        holder.tvTemp.text = "${item.maxTemp}°/${item.minTemp}°"
        holder.tvRain.text = "☁${item.rain}%"

        // SVG load
        ImageLoaderUtil.loadSvgIntoImageView(context, item.icon, holder.imgIcon)
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        val tvRain: TextView = itemView.findViewById(R.id.tvRain)
        val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
    }
}