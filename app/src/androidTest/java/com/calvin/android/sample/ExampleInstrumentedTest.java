package com.calvin.android.sample;

import android.content.Context;
import android.util.Base64;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.calvin.android.secrity.AesCoder;
import com.calvin.android.secrity.DESCoder;
import com.calvin.android.secrity.DESedeCoder;
import com.calvin.android.secrity.DHCoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    //Alice公钥
    private byte[] pubKey1;
    //Alice私钥匙
    private byte[] priKey1;
    //Alice本地密钥
    private byte[] key1;

    //Bob公钥
    private byte[] pubKey2;
    //Bob私钥
    private byte[] priKey2;
    //Bob本地密钥
    private byte[] key2;

    /**
     * 初始化密钥
     * @throws Exception
     */
    @Before
    public void initKey() throws Exception{

        //生成Alice密钥对
        Map<String, Object> keyMap1 = DHCoder.initKey();
        pubKey1 = DHCoder.getPublicKey(keyMap1);
        priKey1 = DHCoder.getPrivateKey(keyMap1);
        System.out.println("Alice公钥：\n "+ Base64.encodeToString(pubKey1, Base64.DEFAULT));
        System.out.println("Alice私钥：\n "+ Base64.encodeToString(priKey1, Base64.DEFAULT));

        //由Alice公钥产生Bob密钥对
        Map<String, Object> keyMap2 = DHCoder.initKey(pubKey1);
        pubKey2 = DHCoder.getPublicKey(keyMap2);
        priKey2 = DHCoder.getPrivateKey(keyMap2);
        System.out.println("Bob公钥：\n "+ Base64.encodeToString(pubKey2, Base64.DEFAULT));
        System.out.println("Bob私钥：\n "+ Base64.encodeToString(priKey2,Base64.DEFAULT));

        //https://blog.csdn.net/fengzun_yi/article/details/104497160
        //java在运用DH密钥交换算法时出现“Unsupported secret key algorithm：AES”错误的解决办法
        key1 = DHCoder.getSecretKey(pubKey2, priKey1);
        System.out.println("Alice本地密钥：\n "+ Base64.encodeToString(key1,Base64.DEFAULT));

        key2 = DHCoder.getSecretKey(pubKey1, priKey2);
        System.out.println("Bob本地密钥：\n "+ Base64.encodeToString(key2,Base64.DEFAULT));

        assertArrayEquals(key1,key2);

    }

    @Test
    public void dhCodecTest() throws Exception{
        System.out.println("\n=====Alice向Bob发送加密数据=====");
        String input1 = "DH密钥交换算法";
        System.out.println("原文："+input1);
        System.out.println("---使用Alice本地密钥对数据加密 ---");
        //使用Alice本地密钥对数据加密
        byte[] code1 = DHCoder.encrypt(input1.getBytes(), key1);
        System.out.println("加密： "+Base64.encodeToString(code1,Base64.DEFAULT));

        System.out.println("---使用Bob本地密钥对数据解密 ---");
        byte[] decode1 = DHCoder.decrypt(code1, key2);
        String output1 = (new String(decode1));

        System.out.println("解密："+output1);

        assertEquals(input1, output1);

        System.out.println("\n=====Bob向Alice发送加密数据=====");

        String input2 = "DH加密算法";
        System.out.println("原文："+input2);
        System.out.println("---使用Bob本地密钥对数据加密 ---");
        //使用Bob本地密钥对数据加密
        byte[] code2 = DHCoder.encrypt(input2.getBytes(), key2);
        System.out.println("加密： "+Base64.encodeToString(code2,Base64.DEFAULT));
        System.out.println("---使用Alice本地密钥对数据解密 ---");

        byte[] decode2 = DHCoder.decrypt(code2, key1);
        String output2 = (new String(decode2));
        System.out.println("解密："+output2);
        assertEquals(input2, output2);
    }

    @Test
    public void desTest() throws Exception{
        String inputStr = "DES加密/解密算法";
        byte[] inputData = inputStr.getBytes();
        System.out.println("原文：\n"+inputStr);
        //初始化密钥
        byte[] key = DESCoder.initKey();
        System.out.println("密钥：\n"+Base64.encodeToString(key, Base64.DEFAULT));
        //加密
        inputData = DESCoder.encrypt(inputData, key);
        System.out.println("加密后：\n"+Base64.encodeToString(inputData, Base64.DEFAULT));

        //解密
        byte[] outputData = DESCoder.decrypt(inputData, key);
        String outputStr = new String(outputData);
        System.out.println("解密后：\n"+outputStr);

        assertEquals(inputStr, outputStr);

    }


    @Test
    public void DESedeTest() throws Exception{
        String inputStr = "DESede加密/解密算法";
        byte[] inputData = inputStr.getBytes();
        System.out.println("原文：\n"+inputStr);
        //初始化密钥
        byte[] key = DESedeCoder.initKey();
        System.out.println("密钥：\n"+Base64.encodeToString(key, Base64.DEFAULT));
        //加密
        inputData = DESedeCoder.encrypt(inputData, key);
        System.out.println("加密后：\n"+Base64.encodeToString(inputData, Base64.DEFAULT));
        //解密
        byte[] outputData = DESedeCoder.decrypt(inputData, key);
        String outputStr = new String(outputData);
        System.out.println("解密后：\n"+outputStr);
        //校验
        assertEquals(inputStr, outputStr);
    }

    @Test
    public void AESTest() throws Exception {
        String inputStr = "AES";
        byte[] inputData = inputStr.getBytes();
        System.out.println("原文：\n"+inputStr);
        System.out.println("长度：\n"+inputData.length);
        //初始化密钥
        byte[] key = AesCoder.initKey();
        System.out.println("密钥：\n"+Base64.encodeToString(key, Base64.DEFAULT));
        //加密
        inputData = AesCoder.encrypt(inputData, key);
        System.out.println("加密后：\n"+Base64.encodeToString(inputData, Base64.DEFAULT));
        System.out.println("长度：\n"+inputData.length);
        //解密
        byte[] outputData = AesCoder.decrypt(inputData, key);
        String outputStr = new String(outputData);
        System.out.println("解密后：\n"+outputStr);
        //校验
        assertEquals(inputStr, outputStr);

    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.calvin.android.sample", appContext.getPackageName());
    }
}
