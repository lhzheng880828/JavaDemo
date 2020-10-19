package com.calvin.android.aop.cache;

import android.text.TextUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import cn.com.superLei.aoparms.AopArms;
import cn.com.superLei.aoparms.annotation.CacheEvict;
import cn.com.superLei.aoparms.common.utils.ArmsCache;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
@Aspect
public class CacheEvictAspect {
    private static final String POINTCUT_METHOD = "execution(@cn.com.xxx.annotation.CacheEvict * *(..))";

    //切点位置，所有的CacheEvict处
    @Pointcut(POINTCUT_METHOD)
    public void onCacheEvictMethod() {
    }

    //环绕处理，并拿到CacheEvict注解值
    @Around("onCacheEvictMethod() && @annotation(cacheEvict)")
    public Object doCacheEvictMethod(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        String key = cacheEvict.key();
        boolean beforeInvocation = cacheEvict.beforeInvocation();
        boolean allEntries = cacheEvict.allEntries();
        ArmsCache aCache = ArmsCache.get(AopArms.getContext());
        Object result = null;
        if (allEntries){
            //如果是全部清空，则key不需要有值
            if (!TextUtils.isEmpty(key))
                throw new IllegalArgumentException("Key cannot have value when cleaning all caches");
            aCache.clear();
        }
        if (beforeInvocation){
            //方法执行前，移除缓存
            aCache.remove(key);
            result = joinPoint.proceed();
        }else {
            //方法执行后，移除缓存，如果出现异常缓存就不会清除（推荐）
            result = joinPoint.proceed();
            aCache.remove(key);
        }
        return result;
    }
}
