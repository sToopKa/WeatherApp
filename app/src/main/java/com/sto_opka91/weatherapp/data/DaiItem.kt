package com.sto_opka91.weatherapp.data

data class DaiItem(
    val sity: String,
    val time: String,
    val condition: String,
    val imageUrl: String,
    val currenttemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)
