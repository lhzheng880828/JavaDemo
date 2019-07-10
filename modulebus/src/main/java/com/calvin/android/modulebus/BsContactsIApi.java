package com.calvin.android.modulebus;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */
public interface BsContactsIApi extends BsIApi {

    String  queryContacts(String number);

    String deleteContacts(int contactId);

    int addContacts(String name, String number);
}
