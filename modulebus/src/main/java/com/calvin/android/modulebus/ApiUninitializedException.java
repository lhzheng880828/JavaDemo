package com.calvin.android.modulebus;

/**
 * Created by zgx on 18-7-2.
 */
/**
 *
 * @en
 * <p>
 * Before call any gs api,make sure the {@link BsApiClient} has been initialized,
 * or else an ApiUninitializedException will occur.
 * <p/>
 * <p>
 * More info refer to {@link ApiException}
 * <p/>
 * @zh
 * <p>
 * 在调用公开的gs接口之前,需要确保{@link BsApiClient}已经初始化
 * 否则当前异常将会发生
 * <p/>
 * <p>
 * 更多信息参考 {@link ApiException}
 * <p/>
 *
 * @device GXV3370, WP820
 * @gsApi
 */
public class ApiUninitializedException extends ApiException {

    /**
     * @en
     * Constructs a new ApiUninitializedException with null params.
     * The specified detail message is defined inside this method.
     * @zh
     * 无参构造方法
     * 异常的详细描述在此接口直接定义好.如日志中有打印此描述便可知道该异常发生了.
     * @device GXV3370, WP820
     * @gsApi
     */
    public ApiUninitializedException() {
        super("Api Uninitialized. Should initialize the ApiClient in application.");
    }
}
