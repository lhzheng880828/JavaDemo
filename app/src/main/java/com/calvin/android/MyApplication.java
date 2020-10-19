package com.calvin.android;

import android.app.Application;

import cn.com.superLei.aoparms.AopArms;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AopArms.init(this);
    }
}
