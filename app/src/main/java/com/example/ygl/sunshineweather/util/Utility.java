package com.example.ygl.sunshineweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.ygl.sunshineweather.db.SunshineWeatherDB;
import com.example.ygl.sunshineweather.model.City;
import com.example.ygl.sunshineweather.model.County;
import com.example.ygl.sunshineweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YGL on 2017/2/13.
 */

public class Utility {
    public synchronized static boolean handleProvincesResponse(SunshineWeatherDB sunshineWeatherDB,String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces=response.split(",");
            if(allProvinces!=null&&allProvinces.length>0){
                for(String line:allProvinces){
                    String[] array=line.split("\\|");
                    Province province=new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    sunshineWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    public synchronized static boolean handleCitiesResponse(SunshineWeatherDB sunshineWeatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities=response.split(",");
            if(allCities!=null&&allCities.length>0){
                for(String line:allCities){
                    String[] array=line.split("\\|");
                    City city=new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    sunshineWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    public synchronized static boolean handleCountiesResponse(SunshineWeatherDB sunshineWeatherDB,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCounties=response.split(",");
            if(allCounties!=null&&allCounties.length>0){
                for(String line:allCounties){
                    String[] array=line.split("\\|");
                    County county=new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    sunshineWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**   * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。   */
    public static void handleWeatherResponse(Context context,String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherlnfo=jsonObject.getJSONObject("weatherinfo");
            String cityName=weatherlnfo.getString("city");
            String weatherCode=weatherlnfo.getString("cityid");
            String temp1=weatherlnfo.getString("temp1");
            String temp2=weatherlnfo.getString("temp2");
            String weatherDesp=weatherlnfo.getString("weather");
            String publishTime=weatherlnfo.getString("ptime");
            saveWeatherlnfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**   * 将服务器返回的所有天气信息存储到SharedPreferences文件中。   */
    public static void saveWeatherlnfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherDesp,String publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        editor.putString("weather_code",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_desp",weatherDesp);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
    }
}
