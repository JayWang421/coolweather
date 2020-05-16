package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.ForecastWeather;
import com.coolweather.android.gson.LifeStyle;
import com.coolweather.android.gson.LifeStyleWeather;
import com.coolweather.android.gson.NowWeather;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView windDirText;

    private TextView windScText;

    private TextView humText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private NowWeather nowWeather ;

    private ForecastWeather forecastWeather;

    private LifeStyleWeather lifeStyleWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各种控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        windDirText =findViewById(R.id.wind_dir_text);
        windScText = findViewById(R.id.wind_sc_text);
        humText = findViewById(R.id.hum_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nowWeatherString = prefs.getString("nowWeather",null);
        String forecastWeatherString = prefs.getString("forecastWeather",null);
        String lifeStyleWeatherString = prefs.getString("lifeStyleWeather",null);
        if(nowWeatherString != null && forecastWeatherString != null && lifeStyleWeatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = new Weather(Utility.handleNowWeatherResponse(nowWeatherString),
                    Utility.handleForcecastWeatherResponse(forecastWeatherString),
                    Utility.handleLifeStyleWeatherResponse(lifeStyleWeatherString));
            showWeatherinfo(weather);
        } else {
            //无缓存时请求天气数据
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据天气ID请求天气数据
     * @param weatherId
     */
    private void requestWeather(String weatherId) {

        String nowWeatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";
        String forecastWeatherUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";
        String lifeStyleWeatherUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";

        //查询当天接口数据
        HttpUtil.sendOkHttpRequest(nowWeatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "nowWeatheronResponse: " + responseText);
                nowWeather = Utility.handleNowWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(nowWeather != null && "ok".equals(nowWeather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("nowWeather",responseText);
                            editor.apply();
                        }
                    }
                });
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //查询未来几天接口数据
        HttpUtil.sendOkHttpRequest(forecastWeatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "forecastWeatheronResponse: " + responseText);
                forecastWeather = Utility.handleForcecastWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(forecastWeather != null && "ok".equals(forecastWeather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("forecastWeather",responseText);
                            editor.apply();
                        }
                    }
                });
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //查询生活指数接口数据
        HttpUtil.sendOkHttpRequest(lifeStyleWeatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, "lifeStyleWeatheronResponse: " + responseText);
                lifeStyleWeather = Utility.handleLifeStyleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(lifeStyleWeather != null && "ok".equals(lifeStyleWeather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("lifeStyleWeather",responseText);
                            editor.apply();
                        }
                    }
                });
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if(nowWeather != null && forecastWeather != null && lifeStyleWeather != null) {
            Weather weather = new Weather(nowWeather, forecastWeather, lifeStyleWeather);
            showWeatherinfo(weather);
        }

    }

    private void showWeatherinfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.nowWeather.now.temperature + "℃";
        String weatherInfo = weather.nowWeather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastWeather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.info);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecastLayout.addView(view);
        }
        windDirText.setText(weather.nowWeather.now.windDir);
        windScText.setText(weather.nowWeather.now.windSc);
        humText.setText(weather.nowWeather.now.hum);
        String comfort = "舒适度：";
        String carWash = "洗车指数：";
        String sport = "运动建议：";
        for (LifeStyle lifeStyle : weather.lifeStyleWeather.lifeStyleList) {
            if("comf".equals(lifeStyle.type)) {
                comfort += lifeStyle.txt;
                comfortText.setText(comfort);
            } else if("cw".equals(lifeStyle.type)) {
                carWash += lifeStyle.txt;
                carWashText.setText(carWash);
            } else if("sport".equals(lifeStyle.type)) {
                sport += lifeStyle.txt;
                sportText.setText(sport);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
