package com.ashu.callapitestcode.data.model;

public class WeatherItem {
    public String time;
    public String iconUrl;
    public float temp;
    public int rain;

    public WeatherItem() {}

    public WeatherItem(String time, String iconUrl, float temp, int rain) {
        this.time = time;
        this.iconUrl = iconUrl;
        this.temp = temp;
        this.rain = rain;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public int getRain() {
        return rain;
    }

    public void setRain(int rain) {
        this.rain = rain;
    }
}
