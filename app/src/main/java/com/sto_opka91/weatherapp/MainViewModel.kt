package com.sto_opka91.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sto_opka91.weatherapp.data.DaiItem

class MainViewModel: ViewModel() {
    val liveDataCurrent = MutableLiveData<DaiItem>()
    val liveDataList = MutableLiveData<List<DaiItem>>()
}