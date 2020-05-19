package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.ForecastWeather;
import com.coolweather.android.gson.LifeStyle;
import com.coolweather.android.gson.LifeStyleWeather;
import com.coolweather.android.gson.NowWeather;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.LogUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

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

    public NowWeather nowWeather ;

    public ForecastWeather forecastWeather;

    public LifeStyleWeather lifeStyleWeather;

    //必应背景图
    private ImageView bingPicImg;

    //当前天气页面的城市Id
    private String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各种控件
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        bingPicImg = findViewById(R.id.bing_pic_img);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nowWeatherString = prefs.getString("nowWeather",null);
        String forecastWeatherString = prefs.getString("forecastWeather",null);
        String lifeStyleWeatherString = prefs.getString("lifeStyleWeather",null);

        if(nowWeatherString != null && forecastWeatherString != null && lifeStyleWeatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = new Weather(Utility.handleNowWeatherResponse(nowWeatherString),
                    Utility.handleForcecastWeatherResponse(forecastWeatherString),
                    Utility.handleLifeStyleWeatherResponse(lifeStyleWeatherString));
            weatherId = weather.nowWeather.basic.weatherId;
            showWeatherinfo(weather);
        } else {
            //无缓存时请求天气数据
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
        //下拉刷新，重新请求天气数据
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        //按钮切换城市天气
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //设置必应每日提供的图片做背景图
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    //加载必应每日一图
    private void loadBingPic() {
        if(!HttpUtil.isNetworkAvailable()) {
            return;
        }
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                if(!TextUtils.isEmpty(bingPic)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

        });
    }

    /**
     * 根据天气ID请求天气数据
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        if(!HttpUtil.isNetworkAvailable()) {
            Toast.makeText(WeatherActivity.this,"网络不可用",Toast.LENGTH_LONG).show();
            //关闭刷新
            swipeRefresh.setRefreshing(false);
            return;
        }
        String nowWeatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=" + HttpUtil.HEFENG_KEY;
        String forecastWeatherUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=" + HttpUtil.HEFENG_KEY;
        String lifeStyleWeatherUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId + "&key=" + HttpUtil.HEFENG_KEY;

        //查询当天接口数据
        HttpUtil.sendOkHttpRequest(nowWeatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
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

        if(nowWeather != null && "ok".equals(nowWeather.status)
                && forecastWeather != null && "ok".equals(forecastWeather.status)
                && lifeStyleWeather != null && "ok".equals(lifeStyleWeather.status)) {
            Weather weather = new Weather(nowWeather, forecastWeather, lifeStyleWeather);
            showWeatherinfo(weather);
            HttpUtil.setRequestNum();
            //关闭刷新
            swipeRefresh.setRefreshing(false);
        } else {
            //数据不全后，每0.5秒请求一次
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(HttpUtil.getRequestNum() > 15) {
                Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                HttpUtil.setRequestNum();
                swipeRefresh.setRefreshing(false);
                return;
            }
            requestWeather(weatherId);
        }
        //获取必应图片
        loadBingPic();

    }

    private void showWeatherinfo(Weather weather) {
        if(weather.nowWeather != null && weather.forecastWeather != null && weather.lifeStyleWeather != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d(TAG, "showWeatherinfo: 天气数据展示 - city:" + weather.nowWeather.basic.cityName +
                    "；updateTime:" + weather.update.updateTime);
            //当前显示的城市Id，下拉刷新时可用到
            this.weatherId = weather.nowWeather.basic.weatherId;
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
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
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
                if ("comf".equals(lifeStyle.type)) {
                    comfort += lifeStyle.txt;
                    comfortText.setText(comfort);
                } else if ("cw".equals(lifeStyle.type)) {
                    carWash += lifeStyle.txt;
                    carWashText.setText(carWash);
                } else if ("sport".equals(lifeStyle.type)) {
                    sport += lifeStyle.txt;
                    sportText.setText(sport);
                }
            }
            weatherLayout.setVisibility(View.VISIBLE);

            //开启后天自动更新天气及每日一图服务
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
}
