package com.calvin.android.modulebus;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public interface BsCallLogIApi extends BsIApi {

    void deleteCalllog(int id);

    void insertCalllog(String callerNum, int account);
}
