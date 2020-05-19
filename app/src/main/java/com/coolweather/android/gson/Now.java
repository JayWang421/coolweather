package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("cond_code")
    public String code;

    @SerializedName("cond_txt")
    public String info;

    @SerializedName("tmp")
    public String temperature;

    public String hum;

    @SerializedName("wind_dir")
    public String windDir;

    @SerializedName("wind_sc")
    public String windSc;

}
