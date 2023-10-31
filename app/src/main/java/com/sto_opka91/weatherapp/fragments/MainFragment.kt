package com.sto_opka91.weatherapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import com.sto_opka91.weatherapp.MainViewModel
import com.sto_opka91.weatherapp.R
import com.sto_opka91.weatherapp.adapters.VpAdapter
import com.sto_opka91.weatherapp.data.DaiItem
import com.sto_opka91.weatherapp.databinding.FragmentMainBinding
import com.sto_opka91.weatherapp.fragments.DaysFragment.DaysFragment
import com.sto_opka91.weatherapp.fragments.HoursFragment.HoursFragment
import com.sto_opka91.weatherapp.utils.DialogManager
import com.sto_opka91.weatherapp.utils.isPermissionGranted
import org.json.JSONObject

const val API_KEY = "3326f41da72e400bbd7133727232310"
class MainFragment : Fragment() {

    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private lateinit var  tList : List<String>
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding : FragmentMainBinding
    private val model: MainViewModel by activityViewModels()
    private lateinit var fLoccationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    private fun init() = with(binding){
        tList = listOf(
            getString(R.string.hours),
            getString(R.string.days)
        )
        val vpAdapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = vpAdapter
        TabLayoutMediator(tlMain, vp){
                tab, pos -> tab.text = tList[pos]
        }.attach()
        fLoccationClient= LocationServices.getFusedLocationProviderClient(requireContext())
        imSync.setOnClickListener {
            tlMain.selectTab(tlMain.getTabAt(0))
            checkLocation()
        }
        imSearch.setOnClickListener {
            DialogManager.searchByName(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }
    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        }else{
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }
    private fun isLocationEnabled(): Boolean{
        val lM = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lM.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    private fun getLocation(){

        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLoccationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
            }
    }
    @SuppressLint("SetTextI18n")
    private fun updateCurrentCard() = with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            tvDate.text = it.time
            tvCity.text = it.sity
            tvCurrentTemp.text = it.currenttemp.ifEmpty {
                "${it.minTemp} C / ${it.maxTemp} С"
            }
            tvTipeWeather.text = it.condition
            tvMaxMinTemp.text = if(it.currenttemp.isEmpty()) "" else  "${it.minTemp} C / ${it.maxTemp} С"
            Picasso.get()
                .load("https:"+it.imageUrl)
                .into(imWeather)
        }
    }
    private fun permissionListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){

        }
    }
    private fun checkPermission(){
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String){
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=3&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                result -> parseWeatherData(result)
            },
            {
                error -> Log.d("myLog", error.message.toString())
            }
        )
        queue.add(request)
    }
    private fun parseWeatherData(result: String){
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }
    private fun parseDays(mainObject: JSONObject): List<DaiItem>{
        val list = ArrayList<DaiItem>()
        val name = mainObject.getJSONObject("location").getString("name")
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        for(i in 0 until daysArray.length()){
            val days = daysArray[i] as JSONObject
            val parceDay = DaiItem(
                name,
                days.getString("date"),
                days.getJSONObject("day").getJSONObject("condition").getString("text"),
                days.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                days.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                days.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                days.getJSONArray("hour").toString()
            )
            list.add(parceDay)
        }
        model.liveDataList.value = list
        return list
    }
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: DaiItem){
        val item = DaiItem(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours
        )
        model.liveDataCurrent.value = item

    }

    companion object {
        fun newInstance() = MainFragment()
    }
}