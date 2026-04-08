package com.ashu.callapitestcode.UILayers

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ashu.callapitestcode.R
import com.ashu.callapitestcode.data.Api.APIDataUri
import com.ashu.callapitestcode.data.Api.APIHelper
import com.ashu.callapitestcode.data.Api.ApiClient
import com.ashu.callapitestcode.data.adapter.ForecastAdapter
import com.ashu.callapitestcode.data.model.ForecastItem
import com.ashu.callapitestcode.data.model.WeatherItem
import com.ashu.callapitestcode.databinding.ActivityMainBinding
import com.ashu.callapitestcode.other.CustomDialog.AshDialog
import com.ashu.callapitestcode.other.LocationHelper
import com.ashu.callapitestcode.other.graphs.AQISeekBar
import com.ashu.callapitestcode.other.graphs.ImageLoaderUtil
import com.ashu.callapitestcode.uitils.TimeUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityData"

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationHelper: LocationHelper

    private val LOCATION_PERMISSION_REQUEST = 100

    private val weatherItemsList = mutableListOf<WeatherItem>()
    private val forecastList = mutableListOf<ForecastItem>()

    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var loadingDialog: AshDialog

    private var isHourlyChecked = true

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TimeUtils.setHomeStatusBarColor(
            window,
            ContextCompat.getColor(this, R.color.toolBarColor)
        )

        // Recycler
        forecastAdapter = ForecastAdapter(this, forecastList)
        binding.recyclerForecast.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerForecast.adapter = forecastAdapter

        loadingDialog = AshDialog(this, "Please wait", "")

        setAqiSeekBar()

        val rootView = findViewById<ViewGroup>(android.R.id.content)

        binding.blurViewCard.setupWith(rootView)
            .setFrameClearDrawable(window.decorView.background)
            .setBlurRadius(20f)
            .setOverlayColor(0x20FFFFFF)

        binding.blurViewCard2.setupWith(rootView)
            .setFrameClearDrawable(window.decorView.background)
            .setBlurRadius(20f)
            .setOverlayColor(0x20FFFFFF)

        getWeatherReportHourly()
        setLineChart()

        locationHelper = LocationHelper(this)
        getLocation()

        // Toggle buttons
        binding.hourlyBtn.setOnClickListener {
            if (!isHourlyChecked) {
                isHourlyChecked = true
                updateToggleUI(true)
                getWeatherReportHourly()
            }
        }

        binding.dailyBtn.setOnClickListener {
            if (isHourlyChecked) {
                isHourlyChecked = false
                updateToggleUI(false)
                getWeatherReportDaily()
            }
        }
    }

    private fun setAqiSeekBar() {

        val customLevels = mutableListOf(
            AQISeekBar.Level(0f, 100f, Color.parseColor("#59B61F"), "Good"),
            AQISeekBar.Level(100f, 200f, Color.parseColor("#EEC732"), "Moderate"),
            AQISeekBar.Level(200f, 300f, Color.parseColor("#EA8C34"), "Poor"),
            AQISeekBar.Level(300f, 400f, Color.parseColor("#E95478"), "Unhealthy"),
            AQISeekBar.Level(400f, 500f, Color.parseColor("#B33FBA"), "Severe"),
            AQISeekBar.Level(500f, 600f, Color.parseColor("#C92033"), "Hazardous")
        )

        val list = mutableListOf(
            AQISeekBar.BottomText("0", "100"),
            AQISeekBar.BottomText(null, "200"),
            AQISeekBar.BottomText(null, "300"),
            AQISeekBar.BottomText(null, "400"),
            AQISeekBar.BottomText(null, "500"),
            AQISeekBar.BottomText(null, "600+")
        )

        binding.aqiSeekBar.apply {
            setBottomTexts(list)
            setBottomMode(AQISeekBar.BOTTOM_CUSTOM)

            setLevels(customLevels)

            // optional controls
            setShowLabels(true)
            setShowBubble(false)
            setUserInteractionEnabled(false)

            setProgress(250f) // ⚠️ Float
        }
    }

    private fun setLineChart() {

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // 🔹 Data
        val entries = arrayListOf(
            Entry(0f, 60f),
            Entry(1f, 40f),
            Entry(2f, 20f),
            Entry(3f, 15f),
            Entry(4f, 35f),
            Entry(5f, 33f),
            Entry(6f, 25f),
            Entry(6f, 55f),
            Entry(6f, 50f),
            Entry(6f, 45f),
            Entry(6f, 40f)
        )

        // 🔹 DataSet
        val dataSet = LineDataSet(entries, "").apply {

            // Line styling
            color = Color.parseColor("#59b61f")
            lineWidth = 4f

            // Circle
            setCircleColor(Color.parseColor("#59b61f"))
            circleRadius = 2f

            // ❌ Shadow remove
            setDrawFilled(false)

            // Smooth curve
            mode = LineDataSet.Mode.CUBIC_BEZIER

            // Values hide
            setDrawValues(false)
        }

        // 🔹 Final Data
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // ================= AXIS =================

        // ❌ Right axis off
        lineChart.axisRight.isEnabled = false

        // ✅ Left axis
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        leftAxis.axisMinimum = 10f
        leftAxis.axisMaximum = 60f
        leftAxis.setLabelCount(6, true)

        // ❌ X-axis grid remove
        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Labels
        val labels = arrayOf("4pm","5pm","6pm","7pm","8pm","9pm","10pm")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f

        // ================= CLEAN UI =================

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setDrawGridBackground(false)

        // animation
        lineChart.animateX(1000)

        // refresh
        lineChart.invalidate()
    }

    private fun updateToggleUI(isHourly: Boolean) {
        if (isHourly) {
            binding.hourlyBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.hourlyBtn.setBackgroundResource(R.drawable.background_bg6)

            binding.dailyBtn.setTextColor(ContextCompat.getColor(this, R.color.text_color_heading))
            binding.dailyBtn.setBackgroundResource(R.drawable.background_bg5)

            binding.weatherGraphLayout.visibility = View.VISIBLE
            binding.recyclerForecast.visibility = View.GONE
        } else {
            binding.dailyBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.dailyBtn.setBackgroundResource(R.drawable.background_bg6)

            binding.hourlyBtn.setTextColor(ContextCompat.getColor(this, R.color.text_color_heading))
            binding.hourlyBtn.setBackgroundResource(R.drawable.background_bg5)

            binding.weatherGraphLayout.visibility = View.GONE
            binding.recyclerForecast.visibility = View.VISIBLE
        }
    }

    // ================= WEATHER =================

    private fun getWeatherReportDaily() {
        ApiClient.api
            .commonGETMethodToHitAllAPIs(
                APIDataUri.getWeatherUrl("14143", "locationId", "daily")
            ).enqueue(object : Callback<JsonObject> {

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    val responseBody = APIHelper.getResponseData(TAG, response, true)

                    try {
                        val forecastArray = responseBody
                            ?.getJSONObject("data")
                            ?.getJSONArray("forecastday") ?: return

                        forecastList.clear()

                        for (i in 0 until forecastArray.length()) {

                            val obj = forecastArray.getJSONObject(i)
                            val dayObj = obj.getJSONObject("day")

                            val item = ForecastItem(
                                day = TimeUtils.convertDateFormat(obj.getString("date"), "EEE"),
                                icon = dayObj.getJSONObject("condition").getString("icon"),
                                maxTemp = dayObj.getDouble("maxtemp_c").toInt(),
                                minTemp = dayObj.getDouble("mintemp_c").toInt(),
                                rain = dayObj.optInt("daily_chance_of_rain")
                            )

                            forecastList.add(item)
                        }

                        forecastAdapter.notifyDataSetChanged()

                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
            })
    }

    private fun getWeatherReportHourly() {
        ApiClient.api
            .commonGETMethodToHitAllAPIs(
                APIDataUri.getWeatherUrl("14143", "locationId", "hourly")
            ).enqueue(object : Callback<JsonObject> {

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    val responseBody = APIHelper.getResponseData(TAG, response, true)

                    try {
                        val hourArray = responseBody
                            ?.getJSONObject("data")
                            ?.getJSONArray("hour") ?: return

                        weatherItemsList.clear()

                        for (i in 0 until hourArray.length()) {

                            val item = hourArray.getJSONObject(i)

                            weatherItemsList.add(
                                WeatherItem(
                                    time = TimeUtils.convertDateFormat(
                                        item.optString("time"),
                                        "hh:mm a"
                                    ),
                                    iconUrl = item.getJSONObject("condition").optString("icon"),
                                    temp = item.optDouble("temp_c").toFloat(),
                                    rain = item.optInt("chance_of_rain")
                                )
                            )
                        }

                        binding.weatherGraph.setData(weatherItemsList)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
            })
    }

    // ================= LOCATION =================

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        if (locationHelper.hasPermission()) {

            locationHelper.fetchLocation(
                onSuccess = { lat, lng ->
                    Log.d(TAG, "Lat: $lat Lng: $lng")
                    setAqiData(lat, lng)
                },
                onError = {
                    Log.e(TAG, it ?: "Error")
                }
            )

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    // ================= AQI =================

    private fun setAqiData(lat: Double, lng: Double) {
        loadingDialog.show()

        ApiClient.api
            .commonGETMethodToHitAllAPIs(
                APIDataUri.getAQIUrl(lat.toString(), lng.toString())
            ).enqueue(object : Callback<JsonObject> {

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    val responseBody = APIHelper.getResponseData(TAG, response, true)

                    try {
                        val responseBody = APIHelper.getResponseData(TAG, response, true)

                        Log.e(TAG, "setAqiData response:- $responseBody")

                        responseBody?.let {

                            val aqiData = it.getJSONArray("data").getJSONObject(0)

                            binding.location.text = aqiData.getString("location")
                            binding.cityState.text =
                                "${aqiData.getString("city")}, ${aqiData.getString("state")}"

                            val iaqi = aqiData.getJSONObject("iaqi")
                            val weather = aqiData.getJSONObject("weather")

                            setAqiValueStatusColor(iaqi.getInt("aqi"))

                            binding.pm2Value.text = iaqi.getString("pm25")
                            binding.pm10Value.text = iaqi.getString("pm10")

                            ImageLoaderUtil.loadSvgIntoImageView(
                                this@MainActivity,
                                weather.getJSONObject("condition").getString("icon"),
                                binding.imageView
                            )

                            binding.temperatureText.text = "${weather.getString("temp_c")}°"
                            binding.weatherStatus.text =
                                weather.getJSONObject("condition").getString("text")
                            binding.humidityValue.text = "${weather.getString("humidity")}%"
                            binding.UVIndexValue.text = weather.getString("uv")
                            binding.WindSpeedValue.text = "${weather.getString("wind_kph")}km/hr"

                            binding.updatedDate.text =
                                TimeUtils.convertDateFormat(
                                    aqiData.getString("updatedAt"),
                                    "yyyy-MM-dd HH:mm"
                                )

                            ImageLoaderUtil.loadSvgIntoImageView(
                                this@MainActivity,
                                aqiData.getString("background_image"),
                                binding.backgroundImage
                            )

                            binding.AQIpm2Value.text = iaqi.getString("pm25")
                            binding.AQIpm10Value.text = iaqi.getString("pm10")

                            // Optional gases
                            if (iaqi.has("so2")) {
                                binding.AQISO2Value.text = iaqi.getString("so2")
                            } else {
                                binding.AQISO2ValueLayout.visibility = ViewGroup.GONE
                            }

                            if (iaqi.has("no2")) {
                                binding.AQINO2Value.text = iaqi.getString("no2")
                            } else {
                                binding.AQINO2ValueLayout.visibility = ViewGroup.GONE
                            }

                            if (iaqi.has("co")) {
                                binding.AQICOValue.text = iaqi.getString("co")
                            } else {
                                binding.AQICOValueLayout.visibility = ViewGroup.GONE
                            }

                            if (iaqi.has("o3")) {
                                binding.AQIO3Value.text = iaqi.getString("o3")
                            } else {
                                binding.AQIO3ValueLayout.visibility = ViewGroup.GONE
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "setAqiData error:- ${e.message}")
                    }

                    loadingDialog.dismiss()
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    loadingDialog.dismiss()
                }
            })
    }

    private fun setAqiValueStatusColor(aqiInt: Int) {

        binding.aqiValue.text = aqiInt.toString()
        binding.aqiSeekBar.setProgress(aqiInt.toFloat())

        var aqiStatus = "Good"
        var aqiStatusBg = R.color.light_good_color
        var aqiColor = R.color.good_color
        var homeBg = R.drawable.home_bg1
        var aqiIcon = R.drawable.good_aqi_icon

        when {
            aqiInt < 100 -> {
                // default (Good)
            }

            aqiInt < 200 -> {
                aqiStatus = "Moderate"
                aqiStatusBg = R.color.light_moderate_color
                aqiColor = R.color.moderate_color
                homeBg = R.drawable.home_bg2
                aqiIcon = R.drawable.moderate_aqi_icon
            }

            aqiInt < 300 -> {
                aqiStatus = "Poor"
                aqiStatusBg = R.color.light_poor_color
                aqiColor = R.color.poor_color
                homeBg = R.drawable.home_bg3
                aqiIcon = R.drawable.poor_aqi_icon
            }

            aqiInt < 400 -> {
                aqiStatus = "Unhealthy"
                aqiStatusBg = R.color.light_unhealthy_color
                aqiColor = R.color.unhealthy_color
                homeBg = R.drawable.home_bg4
                aqiIcon = R.drawable.unhealthy_aqi_icon
            }

            aqiInt < 500 -> {
                aqiStatus = "Severe"
                aqiStatusBg = R.color.light_severe_color
                aqiColor = R.color.severe_color
                homeBg = R.drawable.home_bg5
                aqiIcon = R.drawable.severe_aqi_icon
            }

            else -> {
                aqiStatus = "Hazardous"
                aqiStatusBg = R.color.light_hazardous_color
                aqiColor = R.color.hazardous_color
                homeBg = R.drawable.home_bg6
                aqiIcon = R.drawable.hazardous_aqi_icon
            }
        }

        binding.aqiStatus.text = aqiStatus

        val color = ContextCompat.getColor(this, aqiColor)

        binding.aqiStatus.setTextColor(color)
        binding.aqiValue.setTextColor(color)

        binding.aqiStatus.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, aqiStatusBg))

        binding.centerLayout.setBackgroundResource(homeBg)
        binding.aqiIcon.setImageResource(aqiIcon)
    }

    // ================= PERMISSION =================

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        }
    }
}