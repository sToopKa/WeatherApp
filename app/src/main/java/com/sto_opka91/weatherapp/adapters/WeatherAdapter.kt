package com.sto_opka91.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.sto_opka91.weatherapp.R
import com.sto_opka91.weatherapp.data.DaiItem
import com.sto_opka91.weatherapp.databinding.ListItemBinding

class WeatherAdapter(val listener:Listener?) : ListAdapter<DaiItem,WeatherAdapter.Holder >(Comparator()) {
    class  Holder(view:View, val listener: Listener?) : RecyclerView.ViewHolder(view){
        val binding = ListItemBinding.bind(view)
        var itemTemp: DaiItem? = null
        init{
            itemView.setOnClickListener{
               itemTemp?.let { it1 -> listener?.onClickDays(it1) }
            }
        }

        fun bind(item: DaiItem) = with(binding){
            itemTemp = item
            tvDateItem.text = item.time
            tvConditionItem.text = item.condition
            tvTempItem.text = item.currenttemp.ifEmpty {
                "${item.minTemp} C / ${item.maxTemp} С"
            }
            Picasso.get()
                .load("https:"+item.imageUrl)
                .into(imCondition)
        }
    }
    class Comparator : DiffUtil.ItemCallback<DaiItem>(){
        override fun areItemsTheSame(oldItem: DaiItem, newItem: DaiItem): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: DaiItem, newItem: DaiItem): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClickDays(item: DaiItem)
    }
}