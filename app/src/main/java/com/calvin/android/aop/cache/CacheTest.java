package com.calvin.android.aop.cache;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import cn.com.superLei.aoparms.AopArms;
import cn.com.superLei.aoparms.annotation.CacheEvict;
import cn.com.superLei.aoparms.annotation.TimeLog;
import cn.com.superLei.aoparms.common.utils.ArmsCache;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
public class CacheTest {

    private static final String TAG = "CacheTest";
    public void mainCacheTest() {
        initData();
        getUser();

        removeUser();
        getUser();
    }


    //移除缓存数据
    /**
     * key:缓存的键
     * beforeInvocation:缓存的清除是否在方法之前执行, 如果出现异常缓存就不会清除   默认false
     * allEntries：是否清空所有缓存(与key互斥)  默认false
     */
    @CacheEvict(key = "userList", beforeInvocation = true, allEntries = false)
    private void removeUser() {
        Log.e(TAG, "removeUser: >>>>");
    }

    //缓存数据
    /**
     * key：缓存的键
     * expiry：缓存过期时间,单位s
     * @return 缓存的值
     */
    @TimeLog
    @Cache(key = "userList",expiry = 60 * 60 * 24)
    private ArrayList<User> initData() {
        ArrayList<User> list = new ArrayList<>();
        for (int i=0; i<5; i++){
            User user = new User();
            user.setName("艾神一不小心:"+i);
            user.setPassword("密码:"+i);
            list.add(user);
        }
        return list;
    }

    //获取缓存
    @TimeLog
    private void getUser() {
        ArrayList<User> users = ArmsCache.get(AopArms.getContext()).getAsList("userList", User.class);
        Log.e(TAG, "getUser: "+users);
    }

    public class User implements Serializable {
        private String name;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
