package com.coolweather.android.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);//初始化LitePalApplication
    }

    //获取全局context
    public static Context getContext() {
        return context;
    }
}
