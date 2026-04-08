package com.ashu.callapitestcode.data.model;

public class ForecastItem {
    private String day;
    private String icon;
    private int maxTemp;
    private int minTemp;
    private int rain;

    public ForecastItem(String day, String icon, int maxTemp, int minTemp, int rain) {
        this.day = day;
        this.icon = icon;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.rain = rain;
    }

    public String getDay() { return day; }
    public String getIcon() { return icon; }
    public int getMaxTemp() { return maxTemp; }
    public int getMinTemp() { return minTemp; }
    public int getRain() { return rain; }
}
