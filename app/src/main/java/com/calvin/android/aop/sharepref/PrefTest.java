package com.calvin.android.aop.sharepref;

import android.util.Log;

import cn.com.superLei.aoparms.AopArms;
import cn.com.superLei.aoparms.annotation.Prefs;
import cn.com.superLei.aoparms.annotation.PrefsEvict;
import cn.com.superLei.aoparms.common.utils.ArmsPreference;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-8-20
 */
public class PrefTest {

    private static final String TAG = "PrefTest";

    public void prefMainTest(){

        initArticle();
        getArticle();
        removeArticle();

    }


    //1、保存key到sp
    @Prefs(key = "article")
    private Article initArticle() {
        Article article = new Article();
        article.author = "jerry";
        article.title = "hello android";
        article.createDate = "2019-05-31";
        article.content = "this is a test demo";
        return article;
    }

//2、从sp中移除key
/**
 * key:sp的键
 * allEntries：是否清空所有存储(与key互斥)  默认false
 */
    @PrefsEvict(key = "article", allEntries = false)
    public void removeArticle() {
        Log.e(TAG, "removeArticle: >>>>");
    }

//3、通过key从sp中获取value
    public void getArticle() {
        Article article = ArmsPreference.get(AopArms.getContext(), "article", null);
        Log.e(TAG, "getArticle: "+article);
    }

    private class Article {
        private String author;
        private String title;
        private String createDate;
        private String content;
    }
}
