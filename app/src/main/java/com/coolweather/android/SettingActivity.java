package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.LogUtil;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingActivity";

    //是否开启后台自动更新天气服务
    public boolean isStartService;

    //后台服务更新天气周期为1小时
    public final static int ONEHOUR_UPDATEPERIOD = 60 * 60 * 1000;

    //后台服务更新天气周期为3小时
    public final static int THREEHOUR_UPDATEPERIOD = 3 * 60 * 60 * 1000;

    //后台服务更新天气周期为5小时
    public final static int FIVEHOUR_UPDATEPERIOD = 5 * 60 * 60 * 1000;

    //后台服务更新天气周期为8小时
    public final static int EIGHTHOUR_UPDATEPERIOD = 8 * 60 * 60 * 1000;

    private CheckBox startServiceCheckBox;

    private LinearLayout periodLayout;

    private TextView updatePeriodText;

    private CheckBox onehourPeridCheckBox;

    private CheckBox threehourPeridCheckBox;

    private CheckBox fivehourPeridCheckBox;

    private CheckBox eighthourPeridCheckBox;

    private CheckBox[] checkBoxes = new CheckBox[4];

    private Button backButton;

    //正在运行的周期
    private int runPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        startServiceCheckBox = findViewById(R.id.start_service_checkbox);
        startServiceCheckBox.setOnCheckedChangeListener(this);
        periodLayout = findViewById(R.id.period_layout);
        updatePeriodText = findViewById(R.id.update_period);
        onehourPeridCheckBox = findViewById(R.id.onehour_period_checkbox);
        threehourPeridCheckBox = findViewById(R.id.threehour_period_checkbox);
        fivehourPeridCheckBox = findViewById(R.id.fivehour_period_checkbox);
        eighthourPeridCheckBox = findViewById(R.id.eighthour_period_checkbox);
        backButton = findViewById(R.id.back_button);
        //通过SharedPreferences中的数据判断用户是否点击了启用服务
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isStartService = prefs.getBoolean("isStartService",false);
        startServiceCheckBox.setChecked(isStartService);
        //判断用户之前选择了那个周期
        setPeriodCheckBoxChecked();
        //通过判断用户是否选择开启后台服务来决定周期选项是否显示
        if(startServiceCheckBox.isChecked()) {
            periodLayout.setVisibility(View.VISIBLE);
            updatePeriodText.setVisibility(View.VISIBLE);
        } else {
            periodLayout.setVisibility(View.GONE);
            updatePeriodText.setVisibility(View.GONE);
        }
        checkBoxes[0] = onehourPeridCheckBox;
        checkBoxes[1] = threehourPeridCheckBox;
        checkBoxes[2] = fivehourPeridCheckBox;
        checkBoxes[3] = eighthourPeridCheckBox;
        checkBoxes[0].setOnCheckedChangeListener(this);
        checkBoxes[1].setOnCheckedChangeListener(this);
        checkBoxes[2].setOnCheckedChangeListener(this);
        checkBoxes[3].setOnCheckedChangeListener(this);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        LogUtil.d(TAG,"isChecked : " + isChecked);
        if(isChecked) {
            if(startServiceCheckBox.getText().toString().equals(buttonView.getText().toString())) {//启动服务复选框
                periodLayout.setVisibility(View.VISIBLE);
                updatePeriodText.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean("isStartService",true);
                editor.apply();
            } else {
                //周期复选框，只能选中一个
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].getText().toString().equals(buttonView.getText().toString())) {
                        checkBoxes[i].setChecked(true);
                    } else {
                        checkBoxes[i].setChecked(false);
                    }
                }
                //选中后设定周期
                setUpdatePeriod(buttonView);
            }
        } else {
            if(startServiceCheckBox.getText().toString().equals(buttonView.getText().toString())) {
                periodLayout.setVisibility(View.GONE);
                updatePeriodText.setVisibility(View.GONE);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean("isStartService",false);
                editor.apply();
            } else{
                //周期复选框一个都没选
                if(!checkBoxes[0].isChecked() && !checkBoxes[1].isChecked()
                        && !checkBoxes[2].isChecked() && !checkBoxes[3].isChecked()) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putInt("servicePeriod",0);
                    editor.apply();
                }
            }
        }
    }

    /**
     * 用户选择了哪个周期，就给哪个周期赋值相应的时间
     * @param buttonView
     */
    private void setUpdatePeriod(CompoundButton buttonView) {
        if(onehourPeridCheckBox.getText().toString().equals(buttonView.getText().toString())) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("servicePeriod",ONEHOUR_UPDATEPERIOD);
            editor.apply();
        } else if(threehourPeridCheckBox.getText().toString().equals(buttonView.getText().toString())) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("servicePeriod",THREEHOUR_UPDATEPERIOD);
            editor.apply();
        } else if(fivehourPeridCheckBox.getText().toString().equals(buttonView.getText().toString())) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("servicePeriod",FIVEHOUR_UPDATEPERIOD);
            editor.apply();
        } else if(eighthourPeridCheckBox.getText().toString().equals(buttonView.getText().toString())) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("servicePeriod",EIGHTHOUR_UPDATEPERIOD);
            editor.apply();
        }
    }

    /**
     * 获取用户选择了哪个周期来判断哪个周期复选框被选中
     */
    private void setPeriodCheckBoxChecked() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int choosePeriod = prefs.getInt("servicePeriod",0);
        if(choosePeriod == ONEHOUR_UPDATEPERIOD) {
            onehourPeridCheckBox.setChecked(true);
        } else if(choosePeriod == THREEHOUR_UPDATEPERIOD) {
            threehourPeridCheckBox.setChecked(true);
        } else if(choosePeriod == FIVEHOUR_UPDATEPERIOD) {
            fivehourPeridCheckBox.setChecked(true);
        } else if(choosePeriod == EIGHTHOUR_UPDATEPERIOD) {
            eighthourPeridCheckBox.setChecked(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isStartService = prefs.getBoolean("isStartService",false);
        if(isStartService) {//用户是否选择了开启服务
            int choosePeriod = prefs.getInt("servicePeriod",0);
            if(choosePeriod == 0) {//用户是否选择了周期
                //用户选择开启服务，没选择周期，则默认周期为8小时
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putInt("servicePeriod",EIGHTHOUR_UPDATEPERIOD);
                editor.apply();
            }
            //开启后天自动更新天气及每日一图服务
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            boolean isUpdateServiceRun = isRunService(this,"com.coolweather.android.service.AutoUpdateService");
            if(isUpdateServiceRun) {
                Intent intent = new Intent(this, AutoUpdateService.class);
                stopService(intent);
            }
        }
    }

    /**
     * 判断服务是否在运行
     * @param context
     * @param serviceName 服务的全路径
     * @return
     */
    private boolean isRunService(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

