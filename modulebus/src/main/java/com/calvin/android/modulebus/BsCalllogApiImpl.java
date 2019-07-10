package com.calvin.android.modulebus;

import com.calvin.android.module.annotation.ApiInject;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
@ApiInject("CalllogApi")
public class BsCalllogApiImpl implements BsCallLogIApi {
    @Override
    public void deleteCalllog(int id) {

    }

    @Override
    public void insertCalllog(String callerNum, int account) {

    }

    @Override
    public String getTag() {
        return null;
    }
}
