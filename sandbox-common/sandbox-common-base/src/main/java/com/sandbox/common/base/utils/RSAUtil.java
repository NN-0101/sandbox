package com.sandbox.common.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA 非对称加密工具
 *
 * <p>支持密钥对生成、短文本加解密（≤117 字节，1024 位密钥）和长文本分段加解密
 *
 * @author 0101
 * @since 2026-03-12
 */
public class RSAUtil {

    private static final Logger log = LoggerFactory.getLogger(RSAUtil.class);
    private static final String ALGORITHM = "RSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    private static final int MAX_ENCRYPT_BLOCK_1024 = 117;  // 1024位密钥最大加密块
    private static final int MAX_DECRYPT_BLOCK_1024 = 128;  // 1024位密钥最大解密块

    private RSAUtil() {
    }

    // ==================== 密钥对生成 ====================

    /**
     * 生成 RSA 密钥对
     *
     * @param keySize 密钥长度（如 2048）
     * @return Map 包含 PUBLIC_KEY 和 PRIVATE_KEY
     */
    public static Map<String, Object> initKey(int keySize) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
            gen.initialize(keySize);
            KeyPair pair = gen.generateKeyPair();
            Map<String, Object> map = new HashMap<>(2);
            map.put(PUBLIC_KEY, pair.getPublic());
            map.put(PRIVATE_KEY, pair.getPrivate());
            return map;
        } catch (Exception e) {
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 获取公钥字符串（Base64）
     */
    public static String getPublicKeyStr(Map<String, Object> keyMap) {
        return Base64.getEncoder().encodeToString(((Key) keyMap.get(PUBLIC_KEY)).getEncoded());
    }

    /**
     * 获取私钥字符串（Base64）
     */
    public static String getPrivateKeyStr(Map<String, Object> keyMap) {
        return Base64.getEncoder().encodeToString(((Key) keyMap.get(PRIVATE_KEY)).getEncoded());
    }

    // ==================== 密钥转换 ====================

    /**
     * Base64 字符串转公钥对象
     */
    public static PublicKey getPublicKey(String base64Key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64Key);
        return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
    }

    /**
     * Base64 字符串转私钥对象
     */
    public static PrivateKey getPrivateKey(String base64Key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64Key);
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    // ==================== 单次加解密（短文本）====================

    /**
     * 公钥加密
     *
     * @param text         明文（长度受密钥位数限制）
     * @param publicKeyStr Base64 公钥
     * @return Base64 密文
     */
    public static String encrypt(String text, String publicKeyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
            return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * 私钥解密
     *
     * @param encryptedText Base64 密文
     * @param privateKeyStr Base64 私钥
     * @return 明文
     */
    public static String decrypt(String encryptedText, String privateKeyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    // ==================== 分段加解密（长文本）====================

    /**
     * 公钥分段加密（适用于超长文本）
     */
    public static String encryptSegment(String text, String publicKeyStr) throws Exception {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
        return Base64.getEncoder().encodeToString(doSegment(data, cipher, MAX_ENCRYPT_BLOCK_1024));
    }

    /**
     * 私钥分段解密（适用于超长文本）
     */
    public static String decryptSegment(String encryptedText, String privateKeyStr) throws Exception {
        byte[] data = Base64.getDecoder().decode(encryptedText);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
        return new String(doSegment(data, cipher, MAX_DECRYPT_BLOCK_1024), StandardCharsets.UTF_8);
    }

    // 分段处理核心逻辑
    private static byte[] doSegment(byte[] data, Cipher cipher, int maxBlock) throws Exception {
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        while (offset < inputLen) {
            int length = Math.min(inputLen - offset, maxBlock);
            out.write(cipher.doFinal(data, offset, length));
            offset += maxBlock;
        }
        return out.toByteArray();
    }
}