package com.ashu.callapitestcode.data.Api;

public interface APIDataUri {

    public static String getAQIUrl(String lat, String lon) {
        return "getNearestLocationV2?lat="+ lat +"&long="+ lon +"&type=1";
    }

    public static String getWeatherUrl(String locationid, String searchtype, String type) {
        return "getWeatherDetailsWithForecastApp?locationid="+ locationid +"&searchtype="+ searchtype +"&type=" + type;
    }

}
