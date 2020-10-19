package com.calvin.android.aop.repeat;

import android.util.Log;

import cn.com.superLei.aoparms.annotation.Scheduled;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
public class RepeatTest {
    private static final String TAG = "RepeatTest";

    /**
     * @param interval 初始化延迟
     * @param interval 时间间隔
     * @param timeUnit 时间单位
     * @param count 执行次数
     * @param taskExpiredCallback 定时任务到期回调
     */
    @Scheduled(interval = 1000L, count = 10, taskExpiredCallback = "taskExpiredCallback")
    public void scheduled() {
        Log.e(TAG, "scheduled: >>>>");
    }

    private void taskExpiredCallback(){
        Log.e(TAG, "taskExpiredCallback: >>>>");
    }

}
