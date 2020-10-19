package com.calvin.android.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calvin.android.aop.cache.CacheTest;
import com.calvin.android.aop.repeat.RepeatTest;
import com.calvin.android.aop.retry.RetryTest;
import com.calvin.android.aop.sharepref.PrefTest;
import com.calvin.android.module.annotation.InjectView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import cn.com.superLei.aoparms.annotation.Async;
import cn.com.superLei.aoparms.annotation.Safe;
import cn.com.superLei.aoparms.annotation.SingleClick;
import cn.com.superLei.aoparms.annotation.TimeLog;

public class MainActivity extends AppCompatActivity {




    private static final String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.textView2)
    TextView textView2;

    @InjectView(R.id.submit)
    Button submitBtn;

    @TimeLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate invoke");

        Log.d(TAG,"onCreate invoke");
        Log.d(TAG,"onCreate invoke");
        Log.d(TAG,"onCreate invoke");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent intent = new Intent(MainActivity.this, ExampleActivity.class);
                startActivity(intent);
            }
        });

        aopTest();
    }

    private FloatingActionButton fab;

    @TimeLog
    private void aopTest(){
        CacheTest cacheTest = new CacheTest();
        cacheTest.mainCacheTest();

        PrefTest prefTest = new PrefTest();
        prefTest.prefMainTest();

        //异步
        asyn();
        //trycatch
        safe();
        //retry
        RetryTest retryTest = new RetryTest();
        retryTest.retry();

        RepeatTest repeatTest = new RepeatTest();
        repeatTest.scheduled();


        onclick();
        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                onclick();
            }
        }, 500);
    }

    //value默认500ms
    @SingleClick(value = 2000L)
    private void onclick(){
        Log.e(TAG, "onclick: >>>>");
    }

    //异步
    @Async
    @TimeLog
    public void asyn() {
        Log.e(TAG, "useAync: "+Thread.currentThread().getName());
    }

    //try-catch
    //自动帮你try-catch   允许你定义回调方法
    @Safe(callBack = "throwMethod")
    public void safe() {
        String str = null;
        str.toString();
    }

    //自定义回调方法（注意要和callBack的值保持一致）
    private void throwMethod(Throwable throwable){
        Log.e(TAG, "throwMethod: >>>>>"+throwable.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume invoke");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart invoke");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy invoke");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
