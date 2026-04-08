package com.ashu.callapitestcode.data.model

data class WeatherItem(
    var time: String = "",
    var iconUrl: String = "",
    var temp: Float = 0f,
    var rain: Int = 0
)