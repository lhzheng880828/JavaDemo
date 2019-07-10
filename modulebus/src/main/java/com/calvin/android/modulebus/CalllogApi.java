package com.calvin.android.modulebus;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public class CalllogApi implements BsIApi{

    private static final String TAG = CalllogApi.class.getSimpleName();

    private static BsCallLogIApi getCallLogApi(){
        BsApiClient client = BsApiClient.getInstance();
        if(client == null){
            throw new ApiUninitializedException();
        }
        BsCallLogIApi api = (BsCallLogIApi) client.getApi(TAG);
        return api;
    }

    @Override
    public String getTag() {
        return CalllogApi.class.getSimpleName();
    }
}
