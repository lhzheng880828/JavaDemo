package com.calvin.android.modulebus;

/**
 * Created by zgx on 17-7-4.
 */
/**
 * @en
 * <p>
 * An simple subclass of {@link RuntimeException}.
 * It is unchecked exception.
 * <p/>
 * <p>
 * Unchecked exceptions do not need to be declared in a method or constructor's throws clause
 * if they can be thrown by the execution of the method or constructor
 * and propagate outside the method or constructor boundary.
 * More info refer to {@link RuntimeException}
 * <p/>
 * @zh
 * <p>
 * {@link RuntimeException}的简单子类.
 * 属于无需检查的异常类
 * <p/>
 * <p>
 * 具体参考{@link RuntimeException}
 * <p/>
 * @device GXV3370, WP820
 * @gsApi
 */
public class ApiException extends RuntimeException {
    /**
     * @en
     * Constructs a new ApiException with null as its detail message.
     * @zh
     * 无参构造方法
     * @device GXV3370, WP820
     * @gsApi
     */
    public ApiException() {
        super();
    }

    /**
     * @en
     * Constructs a new ApiException with the specified detail message.
     * @zh
     * 输入一个异常描述可构造一个ApiException
     * @device GXV3370, WP820
     * @gsApi
     */
    public ApiException(String s) {
        super(s);
    }
}
