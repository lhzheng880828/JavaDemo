package com.calvin.android.module.compiler.utils;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public class StringUtil {
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }
}
