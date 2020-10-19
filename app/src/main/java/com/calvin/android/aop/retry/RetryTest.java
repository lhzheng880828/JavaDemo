package com.calvin.android.aop.retry;

import android.util.Log;

import cn.com.superLei.aoparms.annotation.Retry;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
public class RetryTest {
    private static final String TAG = "RetryTest";

    /**
     * @param count 重试次数
     * @param delay 每次重试的间隔
     * @param asyn 是否异步执行
     * @param retryCallback 自定义重试结果回调
     * @return 当前方法是否执行成功
     */
    @Retry(count = 3, delay = 1000, asyn = true, retryCallback = "retryCallback")
    public boolean retry() {
        Log.e(TAG, "retryDo: >>>>>>"+Thread.currentThread().getName());
        return false;
    }

    private void retryCallback(boolean result){
        Log.e(TAG, "retryCallback: >>>>"+result);
    }
}
