package com.ashu.callapitestcode.data.Api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static volatile ApiClient instance; // thread-safe singleton
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;

    private static final long TIMEOUT = 60; // seconds

    private ApiClient() {
        init();
    }

    // 🔥 Singleton instance (thread-safe)
    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    // 🔧 Initialize Retrofit
    private void init() {

        okHttpClient = buildClient();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://apiserver.aqi.in/NoToken/dummy/interview/") // dynamic base url
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 🔧 OkHttp client
    private OkHttpClient buildClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {

                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Content-Type", "application/json");

                    return chain.proceed(builder.build());
                })
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    // 🌐 Get API Service
    public ApiSet getApi() {
        return retrofit.create(ApiSet.class);
    }

}