package com.calvin.android.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */

    /**
     * 编译时注解
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    public @interface ApiInject {
        // 这里定义一个默认接收注解的值；
        // 可以在使用的时候这样调用 @MyAnnotation ("这里填写自定义的值")；
        // 后面我们可以通过Class.getAnnotation(ApiInject.class).getValue();获取这个数据
        String value();
    }
