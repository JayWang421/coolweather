package com.coolweather.android.gson;

public class Weather {

    public String status;

    public Basic basic;

    public Update update;

    public NowWeather nowWeather;

    public ForecastWeather forecastWeather;

    public LifeStyleWeather lifeStyleWeather;

    public Weather(NowWeather nowWeather, ForecastWeather forecastWeather, LifeStyleWeather lifeStyleWeather) {
        this.status = nowWeather.status;
        this.basic = nowWeather.basic;
        this.update = nowWeather.update;
        this.nowWeather = nowWeather;
        this.forecastWeather = forecastWeather;
        this.lifeStyleWeather = lifeStyleWeather;
    }

}
