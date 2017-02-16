package com.example.ygl.sunshineweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ygl.sunshineweather.service.AutoUpdateService;

/**
 * Created by YGL on 2017/2/16.
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent i=new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
