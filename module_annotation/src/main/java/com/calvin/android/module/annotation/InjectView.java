package com.calvin.android.module.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
@Retention(RUNTIME) @Target(FIELD)

public @interface InjectView {
    /** View ID to which the field will be inject. */
    int value();
}
