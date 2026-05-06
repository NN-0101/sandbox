package com.sandbox.postgresql.algorithm;

import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;

/**
 * ShardingSphere 5.5.1 自定义 PostgreSQL AES 加密算法
 * <p>
 * 实现数据库字段级别的 AES 加解密，支持等值查询。
 * 采用 PostgreSQL 兼容的 AES 加密方式（AES/CBC/PKCS5Padding + SHA-256密钥派生）。
 * <p>
 * 配置项：aes-key-value - AES 密钥（必填）
 *
 * @author 0101
 * @since 2026-05-06
 */
public final class PostgresAesEncryptAlgorithm implements EncryptAlgorithm {

    private Properties props;

    @Override
    public void init(final Properties props) {
        this.props = props;
    }

    @Override
    public String encrypt(final Object plaintext, final AlgorithmSQLContext context) {
        if (null == plaintext) {
            return null;
        }
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(plaintext.toString().getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(encrypted);  // 直接返回密文，不需要 IV
        } catch (Exception e) {
            throw new RuntimeException("PostgreSQL AES encrypt error", e);
        }
    }

    @Override
    public Object decrypt(final Object ciphertext, final AlgorithmSQLContext context) {
        if (null == ciphertext) {
            return null;
        }
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] decrypted = cipher.doFinal(Hex.decodeHex(ciphertext.toString().toCharArray()));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("PostgreSQL AES decrypt error", e);
        }
    }

    @Override
    public EncryptAlgorithmMetaData getMetaData() {
        return new EncryptAlgorithmMetaData(true, true, false);
    }

    @Override
    public AlgorithmConfiguration toConfiguration() {
        return new AlgorithmConfiguration(getType(), props);
    }

    private Cipher getCipher(final int mode) throws Exception {
        String key = props.getProperty("aes-key-value");
        if (key == null) {
            throw new IllegalArgumentException("Missing aes-key-value");
        }
        SecretKeySpec secretKey = generatePostgresAesKey(key);

        // 关键修改：使用 ECB 模式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, secretKey);
        return cipher;
    }

    private SecretKeySpec generatePostgresAesKey(final String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(keyBytes, 16), "AES");
    }

    @Override
    public String getType() {
        return "postgresql";
    }
}