package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastWeather {

    public String status;

    public Basic basic;

    public Update update;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
