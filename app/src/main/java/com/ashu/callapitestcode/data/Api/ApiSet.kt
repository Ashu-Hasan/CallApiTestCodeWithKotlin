package com.ashu.callapitestcode.data.Api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiSet {

    @GET
    fun commonGETMethodToHitAllAPIs(
        @Url url: String
    ): Call<JsonObject>
}