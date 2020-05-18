package com.coolweather.android.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/*
HTTP请求的工具类
 */
public class HttpUtil {

    //和风天气key
    public static final String HEFENG_KEY = "0623e6ee4fbe40feb16e098fe4fc8685";

    //请求次数
    static int requestNum = 0;

    public static void sendOkHttpRequest(String address, Callback callback) {
        requestNum++;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static int getRequestNum(){
        return requestNum;
    }

    public static void setRequestNum(){
        requestNum = 0;
    }

}
