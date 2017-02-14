package com.example.ygl.sunshineweather.util;

import android.text.TextUtils;

import com.example.ygl.sunshineweather.db.SunshineWeatherDB;
import com.example.ygl.sunshineweather.model.City;
import com.example.ygl.sunshineweather.model.County;
import com.example.ygl.sunshineweather.model.Province;

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
}
