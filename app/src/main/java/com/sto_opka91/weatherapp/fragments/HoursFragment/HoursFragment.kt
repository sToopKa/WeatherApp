package com.sto_opka91.weatherapp.fragments.HoursFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sto_opka91.weatherapp.MainViewModel
import com.sto_opka91.weatherapp.R
import com.sto_opka91.weatherapp.adapters.WeatherAdapter
import com.sto_opka91.weatherapp.data.DaiItem
import com.sto_opka91.weatherapp.databinding.FragmentHoursBinding
import com.sto_opka91.weatherapp.databinding.FragmentMainBinding
import org.json.JSONArray
import org.json.JSONObject


class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getItemList(it))
        }
    }

    private fun init() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter
    }
    private fun getItemList(item:DaiItem): List<DaiItem>{
        val list = ArrayList<DaiItem>()
        val hoursArray = JSONArray(item.hours)
        for (i in 0 until hoursArray.length()){
            val hour = hoursArray[i] as JSONObject
            val parceHour = DaiItem(
                "",
                hour.getString("time"),
                hour.getJSONObject("condition").getString("text"),
                hour.getJSONObject("condition").getString("icon"),
                hour.getString("temp_c"),
                "",
                "",
                ""
            )
            list.add(parceHour)
        }
        return list
    }
    companion object {

        fun newInstance() = HoursFragment()
    }
}