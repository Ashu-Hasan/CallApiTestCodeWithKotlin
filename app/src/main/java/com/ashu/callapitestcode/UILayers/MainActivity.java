package com.ashu.callapitestcode.UILayers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ashu.callapitestcode.R;
import com.ashu.callapitestcode.data.Api.APIDataUri;
import com.ashu.callapitestcode.data.Api.APIHelper;
import com.ashu.callapitestcode.data.Api.ApiClient;
import com.ashu.callapitestcode.data.adapter.ForecastAdapter;
import com.ashu.callapitestcode.data.model.ForecastItem;
import com.ashu.callapitestcode.data.model.WeatherItem;
import com.ashu.callapitestcode.databinding.ActivityMainBinding;
import com.ashu.callapitestcode.other.CustomDialog.AshDialog;
import com.ashu.callapitestcode.other.LocationHelper;
import com.ashu.callapitestcode.other.graphs.AQISeekBar;
import com.ashu.callapitestcode.other.graphs.ImageLoaderUtil;
import com.ashu.callapitestcode.uitils.TimeUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivityData";
    ActivityMainBinding binding;
    private LocationHelper locationHelper;
    private int LOCATION_PERMISSION_REQUEST = 100;

    List<WeatherItem> weatherItemsList = new ArrayList<>();
    List<ForecastItem> forecastList = new ArrayList<>();
    ForecastAdapter forecastAdapter;

    AshDialog loadingDialog;

    boolean isHourlyChecked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TimeUtils.setHomeStatusBarColor(
                getWindow(),
                ContextCompat.getColor(this, R.color.toolBarColor)
        );


        forecastAdapter = new ForecastAdapter(this, forecastList);

        binding.recyclerForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        binding.recyclerForecast.setAdapter(forecastAdapter);

        loadingDialog = new AshDialog(MainActivity.this, "Please wait", "");


        setAqiSeekBar();

        // Root view (this is correct)
        ViewGroup rootView = findViewById(android.R.id.content);

        binding.blurViewCard.setupWith(rootView)   // ✅ THIS IS CORRECT
                .setFrameClearDrawable(getWindow().getDecorView().getBackground())
                .setBlurRadius(20f)
                .setOverlayColor(0x20FFFFFF);

        binding.blurViewCard2.setupWith(rootView)   // ✅ THIS IS CORRECT
                .setFrameClearDrawable(getWindow().getDecorView().getBackground())
                .setBlurRadius(20f)
                .setOverlayColor(0x20FFFFFF);




       /* weatherItemsList.add(new WeatherItem("4pm", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 17, 10));
        weatherItemsList.add(new WeatherItem("5pm", "https://apiserver.aqi.in/uploads/weather-icons/1.svg", 15, 20));
        weatherItemsList.add(new WeatherItem("6pm", "https://apiserver.aqi.in/uploads/weather-icons/3.svg", 12, 50));
        weatherItemsList.add(new WeatherItem("7pm", "https://apiserver.aqi.in/uploads/weather-icons/4.svg", 10, 80));
        weatherItemsList.add(new WeatherItem("8pm", "https://apiserver.aqi.in/uploads/weather-icons/1.svg", 11, 5));
        weatherItemsList.add(new WeatherItem("9pm", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 14, 80));
        weatherItemsList.add(new WeatherItem("10pm", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 18, 60));
        weatherItemsList.add(new WeatherItem("11pm", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 11, 80));
        weatherItemsList.add(new WeatherItem("12pm", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 9, 90));
        weatherItemsList.add(new WeatherItem("1am", "https://apiserver.aqi.in/uploads/weather-icons/6.svg", 100, 10));*/




        getWeatherReportHourly();


        setLineChart();

        locationHelper = new LocationHelper(this);


        getLocation();

        binding.hourlyBtn.setOnClickListener(view -> {
            if (!isHourlyChecked){
                isHourlyChecked = true;
                binding.hourlyBtn.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                binding.hourlyBtn.setBackgroundResource(R.drawable.background_bg6);

                binding.dailyBtn.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_color_heading));
                binding.dailyBtn.setBackgroundResource(R.drawable.background_bg5);

                binding.weatherGraphLayout.setVisibility(ViewGroup.VISIBLE);
                binding.recyclerForecast.setVisibility(ViewGroup.GONE);

                getWeatherReportHourly();
            }
        });

        binding.dailyBtn.setOnClickListener(view -> {
            if (isHourlyChecked){
                isHourlyChecked = false;
                binding.dailyBtn.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                binding.dailyBtn.setBackgroundResource(R.drawable.background_bg6);

                binding.hourlyBtn.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_color_heading));
                binding.hourlyBtn.setBackgroundResource(R.drawable.background_bg5);

                binding.weatherGraphLayout.setVisibility(ViewGroup.GONE );
                binding.recyclerForecast.setVisibility(ViewGroup.VISIBLE );

                getWeatherReportDaily();
            }
        });

    }

    private void getWeatherReportDaily() {
        ApiClient.getInstance().getApi().commonGETMethodToHitAllAPIs(APIDataUri.getWeatherUrl("14143", "locationId", "daily")).enqueue(new Callback<JsonObject>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JSONObject responseBody = APIHelper.getResponseData(TAG, response, true);

                Log.e(TAG, "getWeatherReport response:- " + responseBody);

                try {
                    if (responseBody.has("data") && responseBody.getJSONObject("data").has("forecastday")){
                        weatherItemsList.clear();
                        JSONArray forecastArray = responseBody
                                .getJSONObject("data")
                                .getJSONArray("forecastday");

                        forecastList.clear();

                        for (int i = 0; i < forecastArray.length(); i++) {

                            JSONObject obj = forecastArray.getJSONObject(i);

                            String date = obj.getString("date");

                            JSONObject dayObj = obj.getJSONObject("day");

                            int maxTemp = (int) dayObj.getDouble("maxtemp_c");
                            int minTemp = (int) dayObj.getDouble("mintemp_c");
                            int rain = dayObj.optInt("daily_chance_of_rain");

                            String icon = dayObj.getJSONObject("condition").getString("icon");

                            // Convert date → Day (Thu, Fri...)
                            String dayName = TimeUtils.convertDateFormat(date, "EEE");

                            forecastList.add(new ForecastItem(dayName, icon, maxTemp, minTemp, rain));
                        }

                        forecastAdapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "getWeatherReport error:- " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {

            }
        });
    }

    private void getWeatherReportHourly() {
        ApiClient.getInstance().getApi().commonGETMethodToHitAllAPIs(APIDataUri.getWeatherUrl("14143", "locationId", "hourly")).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JSONObject responseBody = APIHelper.getResponseData(TAG, response, true);

                Log.e(TAG, "getWeatherReport response:- " + responseBody);

                try {
                    if (responseBody.has("data") && responseBody.getJSONObject("data").has("hour")){
                        weatherItemsList.clear();
                        JSONArray hour = responseBody.getJSONObject("data").getJSONArray("hour");

                        for (int loop = 0; loop < hour.length(); loop++) {

                            JSONObject item = hour.getJSONObject(loop);

                            // 🔍 LOG RAW ITEM
                            Log.d(TAG, "Hour[" + loop + "] Raw: " + item.toString());

                            String time = item.optString("time");
                            double temp = item.optDouble("temp_c");
                            int rain = item.optInt("chance_of_rain");
                            String icon = item
                                    .getJSONObject("condition")
                                    .optString("icon");

                            // 🔍 LOG EACH VALUE
                            Log.d(TAG, "Parsed -> Time: " + time +
                                    " Temp: " + temp +
                                    " Rain: " + rain +
                                    " Icon: " + icon);

                            WeatherItem weatherItem = new WeatherItem();

                            weatherItem.setTime(
                                    TimeUtils.convertDateFormat(time, "hh:mm a")
                            );

                            weatherItem.setIconUrl(icon);

                            // ✅ FIXED (was wrong earlier)
                            weatherItem.setTemp((float) temp);

                            // ✅ add rain (if your model supports it)
                            weatherItem.setRain(rain);

                            weatherItemsList.add(weatherItem);
                        }

                        binding.weatherGraph.setData(weatherItemsList);

                        binding.weatherGraph.setTextSizes(50f, 30f, 30f);
                        binding.weatherGraph.setColors(Color.WHITE, Color.BLACK);
        /*binding.weatherGraph.setVerticalSpacing(
                50f,   // time Y
                120f,  // icon Y
                200f,  // temp Y
                250f,  // graph start
                500f   // rain Y
        );
        binding.weatherGraph.setItemWidth(160f);*/

//        binding.weatherGraph.adjustLayout(40f, 60f, 120f);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "getWeatherReport error:- " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {

            }
        });
    }

    private void setLineChart() {
        LineChart lineChart = findViewById(R.id.lineChart);

// 🔹 Data
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 60));
        entries.add(new Entry(1, 40));
        entries.add(new Entry(2, 20));
        entries.add(new Entry(3, 15));
        entries.add(new Entry(4, 35));
        entries.add(new Entry(5, 33));
        entries.add(new Entry(6, 25));
        entries.add(new Entry(6, 55));
        entries.add(new Entry(6, 50));
        entries.add(new Entry(6, 45));
        entries.add(new Entry(6, 40));

// 🔹 DataSet
        LineDataSet dataSet = new LineDataSet(entries, "");

// Line styling
        dataSet.setColor(Color.parseColor("#3F51B5"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(4f);

// ❌ Shadow remove
        dataSet.setDrawFilled(false);

// Smooth curve
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

// Values hide (clean UI)
        dataSet.setDrawValues(false);

        dataSet.setLineWidth(4f);
        dataSet.setColor(Color.parseColor("#59b61f"));
        dataSet.setCircleRadius(2f); // dots bhi bade kar do
        dataSet.setCircleColor(Color.parseColor("#59b61f"));

// 🔹 Final Data
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);


// ================= AXIS CONTROL =================

// ❌ Right axis off
        lineChart.getAxisRight().setEnabled(false);

// ✅ Left axis (horizontal lines only)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);

        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(10f); // start
        leftAxis.setAxisMaximum(60f); // end
        leftAxis.setLabelCount(6, true);

// ❌ X-axis vertical grid remove
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

// Labels
        String[] labels = {"4pm","5pm","6pm","7pm","8pm","9pm","10pm"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);


// ================= CLEAN UI =================

// remove description
        lineChart.getDescription().setEnabled(false);

// remove legend
        lineChart.getLegend().setEnabled(false);

// remove background grid
        lineChart.setDrawGridBackground(false);

// animation (optional 😏)
        lineChart.animateX(1000);

// refresh
        lineChart.invalidate();
    }

    private void setAqiSeekBar() {
        List<AQISeekBar.Level> customLevels = new ArrayList<>();

        customLevels.add(new AQISeekBar.Level(0, 100, Color.parseColor("#59B61F"), "Good"));
        customLevels.add(new AQISeekBar.Level(100, 200, Color.parseColor("#EEC732"), "Moderate"));
        customLevels.add(new AQISeekBar.Level(200, 300, Color.parseColor("#EA8C34"), "Poor"));
        customLevels.add(new AQISeekBar.Level(300, 400, Color.parseColor("#E95478"), "Unhealthy"));
        customLevels.add(new AQISeekBar.Level(400, 500, Color.parseColor("#B33FBA"), "Severe"));
        customLevels.add(new AQISeekBar.Level(500, 600, Color.parseColor("#C92033"), "Hazardous"));

        List<AQISeekBar.BottomText> list = new ArrayList<>();

        list.add(new AQISeekBar.BottomText("0", "100"));
        list.add(new AQISeekBar.BottomText(null, "200"));
        list.add(new AQISeekBar.BottomText(null, "300"));
        list.add(new AQISeekBar.BottomText(null, "400"));
        list.add(new AQISeekBar.BottomText(null, "500"));
        list.add(new AQISeekBar.BottomText(null, "600+"));

        binding.aqiSeekBar.setBottomTexts(list);
        binding.aqiSeekBar.setBottomMode(AQISeekBar.BOTTOM_CUSTOM);


        binding.aqiSeekBar.setLevels(customLevels);
// optional controls
        binding.aqiSeekBar.setShowLabels(true);
        //  seekBar.setShowBottomText(true);
        binding.aqiSeekBar.setShowBubble(false);
        binding.aqiSeekBar.setUserInteractionEnabled(false);
        binding.aqiSeekBar.setProgress(250);
    }

    private void getLocation() {

        if (locationHelper.hasPermission(MainActivity.this)) {

            locationHelper.fetchLocation(new LocationHelper.LocationCallback() {
                @Override
                public void onLocationReceived(double lat, double lng) {
                    Log.d(TAG, "Lat: " + lat + " Lng: " + lng);
                    setAqiData(lat, lng);
                }

                @Override
                public void onFailed(String message) {
                    Log.e(TAG, message);
                }
            });

        } else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    private void setAqiData(double lat, double lng) {
        loadingDialog.show();
        Log.e(TAG, "setAqiData url:- " + APIDataUri.getAQIUrl(String.valueOf(lat), String.valueOf(lng)));
        ApiClient.getInstance().getApi().commonGETMethodToHitAllAPIs(APIDataUri.getAQIUrl(String.valueOf(lat), String.valueOf(lng))).enqueue(new Callback<JsonObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                try {
                    JSONObject responseBody = APIHelper.getResponseData(TAG, response, true);

                    Log.e(TAG, "setAqiData response:- " + responseBody);

                    if (responseBody != null) {
                        JSONObject aqiData = responseBody.getJSONArray("data").getJSONObject(0);
                        binding.location.setText(aqiData.getString("location"));
                        binding.cityState.setText(aqiData.getString("city") + ", " + aqiData.getString("state"));

                        setAqiValueStatusColor(aqiData.getJSONObject("iaqi").getInt("aqi"));
                        binding.pm2Value.setText(aqiData.getJSONObject("iaqi").getString("pm25"));
                        binding.pm10Value.setText(aqiData.getJSONObject("iaqi").getString("pm10"));

                        ImageLoaderUtil.loadSvgIntoImageView(MainActivity.this, aqiData.getJSONObject("weather").getJSONObject("condition").getString("icon"), binding.imageView);

                        binding.temperatureText.setText(aqiData.getJSONObject("weather").getString("temp_c") + "°");
                        binding.weatherStatus.setText(aqiData.getJSONObject("weather").getJSONObject("condition").getString("text"));
                        binding.humidityValue.setText(aqiData.getJSONObject("weather").getString("humidity") + "%");
                        binding.UVIndexValue.setText(aqiData.getJSONObject("weather").getString("uv"));
                        binding.WindSpeedValue.setText(aqiData.getJSONObject("weather").getString("wind_kph") + "km/hr");
                        binding.updatedDate.setText(TimeUtils.convertDateFormat(aqiData.getString("updatedAt"), "yyyy-MM-dd HH:mm"));

                        ImageLoaderUtil.loadSvgIntoImageView(MainActivity.this, aqiData.getString("background_image"), binding.backgroundImage);

                        binding.AQIpm2Value.setText(aqiData.getJSONObject("iaqi").getString("pm25"));
                        binding.AQIpm10Value.setText(aqiData.getJSONObject("iaqi").getString("pm10"));

                        if (aqiData.getJSONObject("iaqi").has("so2")) {
                            binding.AQISO2Value.setText(aqiData.getJSONObject("iaqi").getString("so2"));
                        }else {
                            binding.AQISO2ValueLayout.setVisibility(ViewGroup.GONE);
                        }
                        if (aqiData.getJSONObject("iaqi").has("no2")) {
                            binding.AQINO2Value.setText(aqiData.getJSONObject("iaqi").getString("no2"));
                        }else {
                            binding.AQINO2ValueLayout.setVisibility(ViewGroup.GONE);
                        }
                        if (aqiData.getJSONObject("iaqi").has("co")) {
                            binding.AQICOValue.setText(aqiData.getJSONObject("iaqi").getString("co"));
                        }
                        else {
                            binding.AQICOValueLayout.setVisibility(ViewGroup.GONE);
                        }
                        if (aqiData.getJSONObject("iaqi").has("o3")) {
                            binding.AQIO3Value.setText(aqiData.getJSONObject("iaqi").getString("o3"));
                        }
                        else {
                            binding.AQIO3ValueLayout.setVisibility(ViewGroup.GONE);
                        }


                    }
                } catch (Exception e) {
                    Log.e(TAG, "setAqiData error:- " + e.getMessage());
                }

                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setAqiValueStatusColor(int aqiInt) {
        binding.aqiValue.setText(String.valueOf(aqiInt));
        binding.aqiSeekBar.setProgress(aqiInt);

        String aqiStatus = "Good";
        int aqiStatusBg = R.color.light_good_color;
        int aqiColor = R.color.good_color;
        int homeBg = R.drawable.home_bg1;
        int aqiIcon = R.drawable.good_aqi_icon;

        if (aqiInt < 100){

        }else if (aqiInt < 200){
            aqiStatus = "Moderate";
            aqiStatusBg = R.color.light_moderate_color;
            aqiColor = R.color.moderate_color;
            homeBg = R.drawable.home_bg2;
            aqiIcon = R.drawable.moderate_aqi_icon;
        }else if (aqiInt < 300){
            aqiStatus = "Poor";
            aqiStatusBg = R.color.light_poor_color;
            aqiColor = R.color.poor_color;
            homeBg = R.drawable.home_bg3;
            aqiIcon = R.drawable.poor_aqi_icon;
        }else if (aqiInt < 400){
            aqiStatus = "Unhealthy";
            aqiStatusBg = R.color.light_unhealthy_color;
            aqiColor = R.color.unhealthy_color;
            homeBg = R.drawable.home_bg4;
            aqiIcon = R.drawable.unhealthy_aqi_icon;
        }else if (aqiInt < 500){
            aqiStatus = "Severe";
            aqiStatusBg = R.color.light_severe_color;
            aqiColor = R.color.severe_color;
            homeBg = R.drawable.home_bg5;
            aqiIcon = R.drawable.severe_aqi_icon;
        }else if (aqiInt < 600){
            aqiStatus = "Hazardous";
            aqiStatusBg = R.color.light_hazardous_color;
            aqiColor = R.color.hazardous_color;
            homeBg = R.drawable.home_bg6;
            aqiIcon = R.drawable.hazardous_aqi_icon;
        }

        binding.aqiStatus.setText(aqiStatus);
        binding.aqiStatus.setTextColor(ContextCompat.getColor(MainActivity.this, aqiColor));
        binding.aqiValue.setTextColor(ContextCompat.getColor(MainActivity.this, aqiColor));
        binding.aqiStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, aqiStatusBg)));
        binding.centerLayout.setBackgroundResource(homeBg);
        binding.aqiIcon.setImageResource(aqiIcon);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}