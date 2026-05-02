package com.sandbox.common.base.utils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加解密工具。
 *
 * <ul>
 *   <li>MySQL 兼容（128位 ECB / Hex）：aesEncrypt / aesDecrypt</li>
 *   <li>标准 CBC（256位 / Base64）：encrypt / decrypt</li>
 *   <li>随机密钥生成：initAESKey()</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-12
 */
public class AesUtil {

    private static final Logger log = LoggerFactory.getLogger(AesUtil.class);
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";
    private static final String KEY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLOP";

    private AesUtil() {
    }

    // ==================== MySQL 兼容（128位 ECB / Hex）====================

    /**
     * 加密，与 MySQL AES_ENCRYPT 兼容
     */
    public static String aesEncrypt(String value, String aesKey) {
        if (value == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, mysqlKey(aesKey));
            return Hex.encodeHexString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("aes encrypt error: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解密，与 MySQL AES_DECRYPT 兼容
     */
    public static String aesDecrypt(String ciphertext, String aesKey) {
        if (ciphertext == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, mysqlKey(aesKey));
            return new String(cipher.doFinal(Hex.decodeHex(ciphertext)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKeySpec mysqlKey(String key) {
        byte[] finalKey = new byte[16];
        int i = 0;
        for (byte b : key.getBytes(StandardCharsets.US_ASCII)) {
            finalKey[i++ % 16] ^= b;
        }
        return new SecretKeySpec(finalKey, "AES");
    }

    // ==================== 标准 CBC（256位 / Base64）====================

    /**
     * AES-CBC 加密，Base64 输出
     */
    public static String encrypt(String data, String secretKey, String ivStr) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, aes256Key(secretKey),
                    new IvParameterSpec(ivStr.getBytes(StandardCharsets.UTF_8)));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("aes encrypt error: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * AES-CBC 解密，Base64 输入
     */
    public static String decrypt(String data, String secretKey, String ivStr) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC);
            cipher.init(Cipher.DECRYPT_MODE, aes256Key(secretKey),
                    new IvParameterSpec(ivStr.getBytes(StandardCharsets.UTF_8)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("aes decrypt error: {}", e.getMessage(), e);
            return null;
        }
    }

    private static SecretKeySpec aes256Key(String key) {
        byte[] finalKey = new byte[32];
        int i = 0;
        for (byte b : key.getBytes(StandardCharsets.US_ASCII)) {
            finalKey[i++ % 32] ^= b;
        }
        return new SecretKeySpec(finalKey, "AES");
    }

    // ==================== 随机密钥 ====================

    /**
     * 生成 32 位随机密钥字符串
     */
    public static String initAESKey() {
        StringBuilder sb = new StringBuilder();
        int len = KEY_CHARS.length();
        for (int i = 0; i < 32; i++) {
            sb.append(KEY_CHARS.charAt((int) Math.round(Math.random() * (len - 1))));
        }
        return sb.toString();
    }
}