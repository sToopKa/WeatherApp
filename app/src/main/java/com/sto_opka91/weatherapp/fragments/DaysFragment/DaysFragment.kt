package com.sto_opka91.weatherapp.fragments.DaysFragment

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
import com.sto_opka91.weatherapp.databinding.FragmentDaysBinding


class DaysFragment : Fragment(), WeatherAdapter.Listener{
private lateinit var binding: FragmentDaysBinding
private lateinit var adapter: WeatherAdapter
private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataList.observe(viewLifecycleOwner){
            adapter.submitList(it.subList(1,it.size))
        }
    }
    private fun init() = with(binding){
        rcViewDays.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(this@DaysFragment)
        rcViewDays.adapter = adapter
    }


    companion object {
        fun newInstance() = DaysFragment()
    }

    override fun onClickDays(item: DaiItem) {
        model.liveDataCurrent.value = item
    }
}