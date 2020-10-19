package com.calvin.android.aop.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.Serializable;

import cn.com.superLei.aoparms.AopArms;
import cn.com.superLei.aoparms.common.utils.ArmsCache;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
@Aspect
public class CacheAspect {
    private static final String POINTCUT_METHOD = "execution(@cn.com.xxx.annotation.Cache * *(..))";

    @Pointcut(POINTCUT_METHOD)
    public void onCacheMethod() {
    }

    @Around("onCacheMethod() && @annotation(cache)")
    public Object doCacheMethod(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
        //获取注解中的key
        String key = cache.key();
        //获取注解中的过期时间
        int expiry = cache.expiry();
        //执行当前注解的方法（放行）
        Object result = joinPoint.proceed();
        //方法执行后进行缓存（缓存对象必须是方法返回值）
        ArmsCache aCache = ArmsCache.get(AopArms.getContext());
        if (expiry>0) {
            aCache.put(key,(Serializable)result,expiry);
        } else {
            aCache.put(key,(Serializable)result);
        }
        return result;
    }
}
