package com.example.ygl.sunshineweather.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ygl.sunshineweather.R;
import com.example.ygl.sunshineweather.service.AutoUpdateService;
import com.example.ygl.sunshineweather.sync.SyncAdapter;
import com.example.ygl.sunshineweather.util.HttpCallbackListener;
import com.example.ygl.sunshineweather.util.HttpUtil;
import com.example.ygl.sunshineweather.util.Utility;

/**
 * Created by YGL on 2017/2/15.
 */

public class WeatherActivity  extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    /**   * 用于显示城市名   */
    private TextView cityNameText;
    /**   * 用于显示发布时间   */
    private TextView publishText;
    /**   * 用于显示天气描述信息   */
    private TextView weatherDespText;
    /**   * 用于显示气温1   */
    private TextView temp1Text;
    /**   * 用于显示气温2   */
    private TextView temp2Text;
    /**   * 用于显示当前日期   */
    private TextView currentDateText;
    /*选择城市按钮*/
    private Button switchCity;
    /*更新天气按钮*/
    private Button refreshWeather;
    @Override
    protected void onCreate(Bundle sls){
        super.onCreate(sls);
        requestWindowFeature((Window.FEATURE_NO_TITLE));
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText=(TextView)findViewById(R.id.city_name);
        publishText=(TextView)findViewById(R.id.publish_text);
        weatherDespText=(TextView)findViewById(R.id.weather_desp);
        temp1Text=(TextView)findViewById(R.id.temp1);
        temp2Text=(TextView)findViewById(R.id.temp2);
        currentDateText=(TextView)findViewById(R.id.current_date);
        switchCity=(Button)findViewById(R.id.switch_city);
        refreshWeather=(Button)findViewById(R.id.refresh_weather);

        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            //如果有县级天气可以查询
            publishText.setText("正在同步");
            //隐藏控件
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);

            queryWeatherCode(countyCode);
        }else{
            //没有县级数据则显示市级数据
            showWeather();
        }

        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        // Create the dummy account
        SyncAdapter.CreateSyncAccount(WeatherActivity.this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent=new Intent(WeatherActivity.this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("正在同步");
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
                String code=prefs.getString("weather_code","");
                if(!TextUtils.isEmpty(code)){
                    queryWeatherlnfo(code);
                }
                break;
            default:
                break;
        }
    }

    /**   * 查询县级代号所对应的天气代号。   */
    private void queryWeatherCode(String countyCode){
        String address= "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServce(address,"countyCode");
    }

    /**   * 查询天气代号所对应的天气。   */
    private void queryWeatherlnfo(String weatherCode){
        String address= "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServce(address,"weatherCode");
    }

    /**   * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。   */
    private void queryFromServce(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String[] array=response.split("\\|");
                        if(array!=null&&array.length==2){
                            String weatherCode=array[1];
                            queryWeatherlnfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    //返回主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**   * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。   */
    private void showWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
        currentDateText.setText(prefs.getString("current_date",""));
        //显示控件
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        //启动后台更新天气服务
        Intent i=new Intent(this, AutoUpdateService.class);
        startService(i);
    }
}
