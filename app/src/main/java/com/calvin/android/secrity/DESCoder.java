package com.calvin.android.secrity;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-10-19
 */
public class DESCoder {
    /**
     * 密钥算法
     * Java 只支持56位密钥
     * Bouncy Castle支持64位
     */
    public static final String KEY_ALGORITHM = "DES";

    /**
     * 加密/解密算法/工作模式/填充模式
     */
    public static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

    /**
     * 转换密钥
     * @param key 二进制密钥
     * @return 密钥
     * @throws Exception 异常
     */
    private static Key toKey(byte[] key) throws Exception {
        //实例化DES密钥材料
        DESKeySpec dks = new DESKeySpec(key);
        //实例化密钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        //生成秘密密钥
        SecretKey secretKey = keyFactory.generateSecret(dks);
        return secretKey;

    }

    /**
     * 解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * @throws Exception 异常
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        ////还原密钥
        Key key1 = toKey(key);
        //实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, key1);
        //执行解密操作
        return cipher.doFinal(data);
    }

    /**
     * 加密
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密数据
     * @throws Exception 异常
     */
    public static  byte[] encrypt(byte[] data, byte[] key) throws  Exception {
        //还原密钥
        Key key1 = toKey(key);
        //实例化加密组件
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key1);
        //执行加密操作
        return cipher.doFinal(data);
    }

    /**
     * 生成密钥
     * Java只支持56位密钥
     * Bouncy Castle支持64位
     * @return byte[] 二进制密钥
     * @throws Exception 异常
     */
    public static byte[] initKey() throws Exception {
        /**
         * 实例化密钥生成器
         * 若要使用64位密钥注意替换将
         * KeyGenerator.getInstance(KEY_ALGORITHM); 替换为
         * KeyGenerator.getInstance(KEY_ALGORITHM, "BC");//使用Bouncy Castle实现
         */
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        /**
         * 初始化密钥生成器
         * 若使用64位密钥注意替换将
         * kg.init(56); 替换为
         * kg.init(64);
         */
        kg.init(56);
        //生成秘密密钥
        SecretKey secretKey = kg.generateKey();
        //获得密钥的二进制编码形式
        return secretKey.getEncoded();

    }
}
