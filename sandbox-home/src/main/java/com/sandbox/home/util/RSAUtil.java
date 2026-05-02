package com.sandbox.home.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA 非对称加密工具类
 * <p>
 * 支持密钥对生成、公钥加密、私钥解密，提供单次和分段两种方式。
 * 1024位密钥最加密117字节/解密128字节；超过限制需使用分段加解密。
 * <p>
 * 注意：RSA 加密速度慢，不适大篇幅加密，建议仅用于加密小数据（如对称密钥）。
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
public class RSAUtil {

    public static final String KEY_ALGORITHM = "RSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 128;

    private RSAUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 密钥对生成 ====================

    public static Map<String, Object> initKey(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(keySize);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("生成密钥对失败", e);
        }
    }

    public static String getPublicKeyStr(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return encryptBASE64(key.getEncoded());
    }

    public static String getPrivateKeyStr(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return encryptBASE64(key.getEncoded());
    }

    // ==================== 密钥转换 ====================

    public static PublicKey getPublicKey(String publicKeyString)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyByte = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKey(String privateKeyString) throws Exception {
        byte[] privateKeyByte = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

    // ==================== 单次加解密 ====================

    /** 公钥加密（适用于数据 ≤ 117 字节） */
    public static String encrypt1(String text, String publicKeyStr) {
        try {
            log.info("明文字符串为:[{}]", text);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
            byte[] tempBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(tempBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + text + "]时遇到异常", e);
        }
    }

    /** 私钥解密（适用于密文 ≤ 128 字节） */
    public static String decrypt1(String secretText, String privateKeyStr) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
            byte[] secretTextDecoded = Base64.getDecoder().decode(secretText.getBytes(StandardCharsets.UTF_8));
            byte[] tempBytes = cipher.doFinal(secretTextDecoded);
            return new String(tempBytes);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + secretText + "]时遇到异常", e);
        }
    }

    // ==================== 分段加解密 ====================

    /** 公钥分段加密（适用于数据 > 117 字节） */
    public static String encrypt2(String plainText, String publicKeyStr) throws Exception {
        log.info("明文:[{}]，长度:[{}]", plainText, plainText.length());
        byte[] plainTextArray = plainText.getBytes(StandardCharsets.UTF_8);
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int inputLen = plainTextArray.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        processDataSegment(plainTextArray, cipher, inputLen, out, 0, 0, MAX_ENCRYPT_BLOCK);

        byte[] encryptText = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptText);
    }

    /** 私钥分段解密（适用于密文 > 128 字节） */
    public static String decrypt2(String encryptTextHex, String privateKeyStr) throws Exception {
        byte[] encryptText = Base64.getDecoder().decode(encryptTextHex);
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        int inputLen = encryptText.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        processDataSegment(encryptText, cipher, inputLen, out, 0, 0, MAX_DECRYPT_BLOCK);

        out.close();
        return out.toString();
    }

    private static void processDataSegment(byte[] data, Cipher cipher, int inputLen,
                                           ByteArrayOutputStream out, int offSet, int i,
                                           int maxBlockSize) throws IllegalBlockSizeException, BadPaddingException {
        byte[] cache;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(data, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
    }

    public static void main(String[] args) throws Exception {
        String content = "mytHbFdzFzkncsGcGIFnHjFDhoCtFdin";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuH/2rSugnzOMFxvYIUUl/RDnUpin7UL7Ko9ZGHTH1gE7ArYQibQhV0pCUOxdn71chSKtIc0hqZ9u8WvGtA2Rb4Ck5CNFqN4GcWhF1KxR5d6xg0DU8ENkHMwr5/E2IKRQ49H5TVAteCFGsi2SzCQZIlUg/m/jtoJB8wL+vF8skfwIDAQAB";
        String encrypted = encrypt1(content, publicKey);
        log.info("加密后的密文:[{}]，长度:[{}]", encrypted, encrypted.length());
    }
}