package com.example.ygl.sunshineweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ygl.sunshineweather.R;
import com.example.ygl.sunshineweather.db.SunshineWeatherDB;
import com.example.ygl.sunshineweather.model.City;
import com.example.ygl.sunshineweather.model.County;
import com.example.ygl.sunshineweather.model.Province;
import com.example.ygl.sunshineweather.util.HttpCallbackListener;
import com.example.ygl.sunshineweather.util.HttpUtil;
import com.example.ygl.sunshineweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YGL on 2017/2/14.
 */

public class ChooseAreaActivity extends Activity {
    private ListView listView;
    private TextView titleText;
    private ArrayAdapter adapter;
    private List<String> dataList=new ArrayList<String>();
    private SunshineWeatherDB sunshineWeatherDB;
    private ProgressDialog progressDialog;

    private int currentl_evel;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    /**   * 省列表   */
    private List<Province> provinceList;
    /**   * 市列表   */
    private List<City> cityList;
    /**   * 县列表   */
    private List<County> countyList;
    /**   * 选中的省份   */
    private Province selectedProvince;
    /**   * 选中的城市   */
    private City selectedCity;
    /**   * 是否从WeatherActivity中跳转过来。   */
    private Boolean isFromWeatherActivity;
    @Override
    protected void onCreate(Bundle sls){
        super.onCreate(sls);
        isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)&&!isFromWeatherActivity){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);//设置为没有工具栏
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText=(TextView)findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        sunshineWeatherDB=SunshineWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentl_evel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    //读取城市
                    queryCities();
                }else{
                    if(currentl_evel==LEVEL_CITY){
                        selectedCity=cityList.get(position);
                        //读取县市信息
                        queryCounties();
                    }else {
                        if(currentl_evel==LEVEL_COUNTY){
                            String countyCode=countyList.get(position).getCountyCode();
                            Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                            intent.putExtra("county_code",countyCode);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }
        });
        //加载省级数据
        queryProvinces();
    }

    /**   * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。   */
    private void queryProvinces(){
        provinceList=sunshineWeatherDB.loadProvinces();
        if(!(provinceList.size()>0)){
            //从服务器读取省级数据
            queryFromServer(null,"province");
        }else{
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
        }   adapter.notifyDataSetChanged();
        listView.setSelection(0);
        titleText.setText("中国");
        currentl_evel=LEVEL_PROVINCE;
    }

    /**   * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。   */
    private void queryCities(){
        cityList=sunshineWeatherDB.loadCities(selectedProvince.getId());
        if(!(cityList.size()>0)){
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }else{
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentl_evel=LEVEL_CITY;
        }
    }

    /**   * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。   */
    private  void  queryCounties(){
        countyList=sunshineWeatherDB.loadCounties(selectedCity.getId());
        if(!(countyList.size()>0)){
            queryFromServer(selectedCity.getCityCode(),"county");
        }else{
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentl_evel=LEVEL_COUNTY;
        }
    }

    /**   * 根据传入的代号和类型从服务器上查询省市县数据。   */
    private void queryFromServer(final String code,final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvincesResponse(sunshineWeatherDB,response);
                }else if("city".equals(type)){
                    result=Utility.handleCitiesResponse(sunshineWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountiesResponse(sunshineWeatherDB,response,selectedCity.getId());
                }

                if(result){
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**   * 显示进度对话框   */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**   * 关闭进度对话框   */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*
     * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentl_evel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentl_evel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
