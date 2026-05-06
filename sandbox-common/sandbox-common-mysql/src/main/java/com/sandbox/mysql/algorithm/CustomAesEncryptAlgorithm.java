package com.sandbox.mysql.algorithm;

import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * ShardingSphere 5.5.1 自定义 AES 加密算法
 * <p>
 * 实现数据库字段级别的 AES 加解密，支持等值查询。
 * 采用 MySQL 兼容的 AES 密钥生成方式。
 * <p>
 * 配置项：aes-key-value - AES 密钥（必填）
 *
 * @author 0101
 * @since 2026-05-06
 */
public final class CustomAesEncryptAlgorithm implements EncryptAlgorithm {

    private Properties props;

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    /**
     * 加密：明文 → 十六进制密文
     */
    @Override
    public String encrypt(final Object plaintext, final AlgorithmSQLContext context) {
        if (null == plaintext) {
            return null;
        }
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] result = cipher.doFinal(plaintext.toString().getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encodeHex(result));
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt error", e);
        }
    }

    /**
     * 解密：十六进制密文 → 明文
     */
    @Override
    public Object decrypt(final Object ciphertext, final AlgorithmSQLContext context) {
        if (null == ciphertext) {
            return null;
        }
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] result = cipher.doFinal(Hex.decodeHex(ciphertext.toString().toCharArray()));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt error", e);
        }
    }

    /**
     * 算法元数据：支持解密、等值查询，不支持模糊查询
     */
    @Override
    public EncryptAlgorithmMetaData getMetaData() {
        return new EncryptAlgorithmMetaData(true, true, false);
    }

    @Override
    public AlgorithmConfiguration toConfiguration() {
        return new AlgorithmConfiguration(getType(), props);
    }

    /**
     * 获取 AES Cipher
     */
    private Cipher getCipher(final int mode) throws Exception {
        String key = props.getProperty("aes-key-value");
        if (key == null) {
            throw new IllegalArgumentException("Missing aes-key-value");
        }
        SecretKeySpec secretKey = generateMysqlAesKey(key);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, secretKey);
        return cipher;
    }

    /**
     * MySQL 兼容的 AES 密钥生成：对原始 key 做异或折叠到 16 字节
     */
    private SecretKeySpec generateMysqlAesKey(final String key) {
        byte[] finalKey = new byte[16];
        byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < keyBytes.length; i++) {
            finalKey[i % 16] ^= keyBytes[i];
        }
        return new SecretKeySpec(finalKey, "AES");
    }

    @Override
    public String getType() {
        return "mysql";
    }
}