package com.sandbox.home.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * AES 加密解密工具类
 * <p>
 * 提供 MySQL 兼容模式（128位，ECB，Hex）和标准 AES-CBC 模式（256位，CBC/PKCS5Padding，Base64）。
 * MySQL 模式用于数据库字段加解密，CBC 模式用于通用数据加密。
 * <p>
 * 注意：ECB 模式安全性低，仅用于兼容已有 MySQL 加密数据；生产环境 CBC 模式应随机生成 IV。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
public class AesUtil {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLOP";

    private AesUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== MySQL 兼容模式 ====================

    /**
     * MySQL 兼容的 AES 加密（128位密钥，ECB 模式，无 IV）
     *
     * @param value  明文字符串，为 null 返回 null
     * @param aesKey 加密密钥
     * @return Hex 编码密文，失败返回 null
     */
    public static String aesEncrypt(String value, String aesKey) {
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            SecretKeySpec key = generateMysqlAesKey(aesKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cleartext = value.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = cipher.doFinal(cleartext);
            return new String(Hex.encodeHex(ciphertextBytes));
        } catch (Exception e) {
            log.error(String.format("aes_encrypt error, content= %s, errorMsg= %s", value, e.getMessage()), e);
        }
        return null;
    }

    /**
     * MySQL 兼容的 AES 解密
     *
     * @param ciphertext Hex 编码密文，为 null 返回 null
     * @param aesKey     解密密钥
     * @return 明文字符串，失败返回 null
     */
    public static String aesDecrypt(String ciphertext, String aesKey) {
        try {
            if (null == ciphertext) {
                return null;
            } else {
                SecretKey key = generateMysqlAesKey(aesKey);
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] cleartext = Hex.decodeHex(ciphertext.toCharArray());
                byte[] ciphertextBytes = cipher.doFinal(cleartext);
                return new String(ciphertextBytes, StandardCharsets.UTF_8);
            }
        } catch (Throwable var5) {
            return null;
        }
    }

    /**
     * 生成 MySQL 兼容的 128 位密钥（通过异或运算派生）
     */
    private static SecretKeySpec generateMysqlAesKey(String key) {
        byte[] finalKey = new byte[16];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);
        int var5 = var4.length;

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 16] ^= b;
        }

        return new SecretKeySpec(finalKey, "AES");
    }

    // ==================== 标准 AES-CBC 模式 ====================

    /**
     * AES-CBC 加密（256位密钥，PKCS5Padding）
     *
     * @param data      明文字符串，为 null 返回 null
     * @param secretKey 密钥字符串，派生为 32 字节
     * @param ivStr     IV 向量，需 16 字节
     * @return Base64 编码密文，失败返回 null
     */
    public static String encrypt(String data, String secretKey, String ivStr) {
        if (Objects.isNull(data)) {
            return null;
        }
        try {
            SecretKeySpec key = generateAesKey(secretKey);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes(UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            log.error(String.format("aes_encrypt error, content= %s, errorMsg= %s", data, e.getMessage()), e);
        }
        return null;
    }

    /**
     * AES-CBC 解密
     *
     * @param encryptedData Base64 编码密文，为 null 返回 null
     * @param secretKey     密钥字符串
     * @param ivStr         IV 向量，需 16 字节
     * @return 明文字符串，失败返回 null
     */
    public static String decrypt(String encryptedData, String secretKey, String ivStr) {
        if (Objects.isNull(encryptedData)) {
            return null;
        }
        try {
            SecretKeySpec key = generateAesKey(secretKey);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes(UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(String.format("aes_decrypt error, content= %s, errorMsg= %s", encryptedData, e.getMessage()), e);
        }
        return null;
    }

    /**
     * 生成 256 位 AES 密钥（通过异或运算派生）
     */
    private static SecretKeySpec generateAesKey(String key) {
        byte[] finalKey = new byte[32];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);
        int var5 = var4.length;

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 32] ^= b;
        }

        return new SecretKeySpec(finalKey, "AES");
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成 32 字符随机 AES 密钥字符串
     */
    public static String initAESKey() {
        StringBuilder sb = new StringBuilder();
        int len = AES_STRING.length();
        for (int i = 0; i < 32; i++) {
            sb.append(AES_STRING.charAt(getRandom(len - 1)));
        }
        return sb.toString();
    }

    private static int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }


    public static void main(String[] args) {
        String str = encrypt("895632",
                "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08",
                "DboFssEOkcKDygyK");
        System.out.println("Encrypted data: " + str);

        String str2 = decrypt(str,
                "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08",
                "DboFssEOkcKDygyK");
        System.out.println("Decrypted data: " + str2);
    }
}