package com.ashu.callapitestcode.data.model

data class ForecastItem(
    val day: String,
    val icon: String,
    val maxTemp: Int,
    val minTemp: Int,
    val rain: Int
)