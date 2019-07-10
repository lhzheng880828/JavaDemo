package com.calvin.android.modulebus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public class BsApiClient {

    private static final boolean DEBUG = true;

    private static final String TAG = BsApiClient.class.getSimpleName();

    private static Map<String, BsIApi> apiMap = new HashMap<>();

    static {
        BsIApi contactApi = getBsApi("com.calvin.android.modulebus.BsContactsApiImpl");
        String contactApiKey = getBsApiKey("com.calvin.android.modulebus.ContactsApi");
        apiMap.put(contactApiKey, contactApi);

        BsIApi callLogApi = getBsApi("com.calvin.android.modulebus.BsCalllogApiImpl");
        String callLogApiKey = getBsApiKey("com.calvin.android.modulebus.CalllogApi");
        apiMap.put(callLogApiKey, callLogApi);
    }

    private BsApiClient(){

    }

    private static BsApiClient instance;

    public static BsApiClient getInstance(){
        if(instance==null){
            instance = new BsApiClient();
        }
        return instance;
    }

    public BsIApi getApi(String api) {
        if (apiMap.get(api)==null) {
            throw new ApiException("Must first call addApi(" + api + ") when initializing");
        }
        return apiMap.get(api);
    }

    private static String getBsApiKey(String packageName) {
        try {
            Class<?> c = Class.forName(packageName);
            Field apiField = c.getField("API");
            if (apiField != null) {
                return apiField.get(c.newInstance()).toString();
            }
        } catch (ClassNotFoundException e) {
            if (DEBUG) System.out.println(TAG+",getApiKey from " + packageName + ",error=" + e.toString());
        } catch (NoSuchFieldException e) {
            if (DEBUG) System.out.println(TAG+", getApiKey from " + packageName + ",error=" + e.toString());
        } catch (InstantiationException e) {
            if (DEBUG) System.out.println(TAG+", getApiKey from " + packageName + ",error=" + e.toString());
        } catch (IllegalAccessException e) {
            if (DEBUG) System.out.println(TAG+", getApiKey from " + packageName + ",error=" + e.toString());
        }
        return null;
    }

    private static BsIApi getBsApi(String packageName) {
        try {
            Class<?> c = Class.forName(packageName);
            return (BsIApi) c.newInstance();
        } catch (ClassNotFoundException e) {
            if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
        } catch (InstantiationException e) {
            if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
        } catch (IllegalAccessException e) {
            if (DEBUG) System.out.println(TAG+", getApiIApi from " + packageName + ",error=" + e.toString());
        }
        return null;
    }


}
