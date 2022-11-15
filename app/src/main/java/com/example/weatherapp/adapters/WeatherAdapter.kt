package com.example.weatherapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.dataClasses.DayWeather
import com.example.weatherapp.databinding.ListItem1Binding
import com.example.weatherapp.databinding.ListItemSmallBinding
import com.squareup.picasso.Picasso


class WeatherAdapter : ListAdapter<DayWeather, WeatherAdapter.Holder>(Comparator()) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ListItem1Binding.bind(view)

        @SuppressLint("SetTextI18n")
        fun bind(item: DayWeather) = with(binding) {
            tVDate.text = item.dateTime
            //tVCondition.text = item.condition
            tVTemp.text = item.currentTemperature.ifEmpty { "${item.maxTemperature}/${item.minTemperature}"} + "°C"
            Picasso.get().load("https:"+item.imgUrl).into(imWeather)
        }
    }

    class Comparator : DiffUtil.ItemCallback<DayWeather>() {
        override fun areItemsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item1, parent, false
        )

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
}