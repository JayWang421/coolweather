package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.WeatherActivity;
import com.coolweather.android.gson.ForecastWeather;
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

public class AutoUpdateService extends Service {

    private static final String TAG = "AutoUpdateService";

    NowWeather nowWeather;

    ForecastWeather forecastWeather;

    LifeStyleWeather lifeStyleWeather;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nowWeather = null;
        forecastWeather = null;
        lifeStyleWeather = null;
        updateWeather();
        updateBingPic();
        Log.d(TAG, "onStartCommand: 后台服务更新，nowWeather:cityName:" + nowWeather.basic.cityName + "；updateTime:" + nowWeather.update.updateTime);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;//8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + 60 * 1000;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nowWeatherString = prefs.getString("nowWeather",null);
        String forecastWeatherString = prefs.getString("forecastWeather",null);
        String lifeStyleWeatherString = prefs.getString("lifeStyleWeather",null);

        if(nowWeatherString != null && forecastWeatherString != null && lifeStyleWeatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = new Weather(Utility.handleNowWeatherResponse(nowWeatherString),
                    Utility.handleForcecastWeatherResponse(forecastWeatherString),
                    Utility.handleLifeStyleWeatherResponse(lifeStyleWeatherString));
            String weatherId = weather.nowWeather.basic.weatherId;
            String nowWeatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";
            String forecastWeatherUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";
            String lifeStyleWeatherUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=c41075c0e3e4481ea10ebead32107f13";

            //查询当天接口数据
            HttpUtil.sendOkHttpRequest(nowWeatherUrl, new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseText = response.body().string();
                    nowWeather = Utility.handleNowWeatherResponse(responseText);
                    if(nowWeather != null && "ok".equals(nowWeather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("nowWeather",responseText);
                        editor.apply();
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });

            //查询未来几天接口数据
            HttpUtil.sendOkHttpRequest(forecastWeatherUrl, new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseText = response.body().string();
                    forecastWeather = Utility.handleForcecastWeatherResponse(responseText);
                    if(forecastWeather != null && "ok".equals(forecastWeather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("forecastWeather",responseText);
                        editor.apply();
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });

            //查询生活指数接口数据
            HttpUtil.sendOkHttpRequest(lifeStyleWeatherUrl, new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseText = response.body().string();
                    lifeStyleWeather = Utility.handleLifeStyleWeatherResponse(responseText);
                    if(lifeStyleWeather != null && "ok".equals(lifeStyleWeather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("lifeStyleWeather",responseText);
                        editor.apply();
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });
        }

        //更新失败后重新更新
        if(nowWeather == null || !"ok".equals(nowWeather.status) ||
                forecastWeather == null || !"ok".equals(forecastWeather.status) ||
                lifeStyleWeather == null || !"ok".equals(lifeStyleWeather.status)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateWeather();
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                if(!TextUtils.isEmpty(bingPic)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }
}
