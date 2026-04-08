package com.ashu.callapitestcode.data.Api

object APIDataUri {

    fun getAQIUrl(lat: String, lon: String): String {
        return "getNearestLocationV2?lat=$lat&long=$lon&type=1"
    }

    fun getWeatherUrl(locationId: String, searchType: String, type: String): String {
        return "getWeatherDetailsWithForecastApp?locationid=$locationId&searchtype=$searchType&type=$type"
    }
}