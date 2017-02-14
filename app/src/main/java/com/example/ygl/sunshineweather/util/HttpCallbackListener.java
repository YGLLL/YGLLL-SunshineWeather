package com.example.ygl.sunshineweather.util;

/**
 * Created by YGL on 2017/2/13.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
