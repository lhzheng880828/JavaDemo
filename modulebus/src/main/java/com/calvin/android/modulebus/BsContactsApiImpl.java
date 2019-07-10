package com.calvin.android.modulebus;

import com.calvin.android.module.annotation.ApiInject;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-4
 */

@ApiInject("ContactsApi")
public class BsContactsApiImpl implements BsContactsIApi {
    @Override
    public String queryContacts(String number) {
        return null;
    }

    @Override
    public String deleteContacts(int contactId) {
        return null;
    }

    @Override
    public int addContacts(String name, String number) {
        return 0;
    }

    @Override
    public String getTag() {
        return null;
    }
}
