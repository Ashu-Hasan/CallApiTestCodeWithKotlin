package com.ashu.callapitestcode.data.Api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiSet {
    @GET
    Call<JsonObject> commonGETMethodToHitAllAPIs(@Url String url);
}
