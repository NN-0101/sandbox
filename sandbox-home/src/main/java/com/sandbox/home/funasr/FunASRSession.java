package com.sandbox.home.funasr;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * FunASR 单设备会话
 * <p>
 * 职责：
 * <ul>
 *   <li>维护与 FunASR 服务器的 WebSocket 连接（一设备一会话）</li>
 *   <li>发送 PCM 音频数据和语音结束标识</li>
 *   <li>接收识别结果，通过回调通知上层</li>
 * </ul>
 *
 * <h3>生命周期</h3>
 * <pre>
 * new FunASRSession() → start() → sendAudio() → finishSpeaking() → close()
 * </pre>
 */
@Slf4j
public class FunASRSession {

    @Getter
    private final String deviceId;
    private final String wsUrl;
    private WebSocketClient client;
    private volatile boolean isConnected = false;

    /**
     * 连接就绪信号：onOpen 配置发送完成后释放
     */
    private final CountDownLatch readyLatch = new CountDownLatch(1);

    /**
     * 识别结果回调（不可变，创建时绑定）
     */
    private final Consumer<String> recognitionCallback;

    public FunASRSession(String deviceId, String wsUrl, Consumer<String> callback) {
        this.deviceId = deviceId;
        this.wsUrl = wsUrl;
        this.recognitionCallback = callback;
    }

    /**
     * 启动会话：建立 WebSocket 连接，等待 onOpen 发送配置后返回
     */
    public synchronized void start() {
        if (isConnected) {
            return;
        }
        connectWebSocket();
        try {
            if (readyLatch.await(5, TimeUnit.SECONDS)) {
                log.info("设备 {} 的 FunASR 会话就绪", deviceId);
            } else {
                log.warn("设备 {} 的 FunASR 会话等待超时", deviceId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("设备 {} 的 FunASR 会话等待被中断", deviceId);
        }
    }

    /**
     * 发送 PCM 音频数据
     */
    public void sendAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return;
        }
        if (isConnected && client != null && client.isOpen()) {
            try {
                client.send(ByteBuffer.wrap(audioData));
            } catch (Exception e) {
                log.error("设备 {} 发送音频数据失败", deviceId, e);
            }
        } else {
            log.warn("设备 {} 的 FunASR 未连接，丢弃 {} 字节音频", deviceId, audioData.length);
        }
    }

    /**
     * 发送语音结束标识
     * <p>携带 wav_name，与 onOpen 配置保持一致，确保 FunASR 返回结果时正确关联设备
     */
    public void finishSpeaking() {
        if (isConnected && client != null && client.isOpen()) {
            client.send(buildConfig(false));
            log.info("设备 {} 发送语音结束标识", deviceId);
        }
    }

    /**
     * 关闭会话，释放 WebSocket 连接
     */
    public synchronized void close() {
        log.info("关闭设备 {} 的 FunASR 会话", deviceId);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) { /* ignore */ }
            client = null;
        }
        isConnected = false;
    }

    /**
     * 检查连接是否存活
     */
    public boolean isConnected() {
        return isConnected && client != null && client.isOpen();
    }

    // ==================== 私有方法 ====================

    /**
     * 构建 FunASR 配置 JSON
     *
     * @param isSpeaking true-开始说话，false-结束说话
     */
    private String buildConfig(boolean isSpeaking) {
        return "{"
                + "\"mode\":\"2pass\","
                + "\"chunk_size\":[5,10,5],"
                + "\"wav_format\":\"pcm\","
                + "\"audio_fs\":16000,"
                + "\"is_speaking\":" + isSpeaking + ","
                + "\"wav_name\":\"" + deviceId + "\""
                + "}";
    }

    /**
     * 建立 WebSocket 连接并绑定事件
     */
    private void connectWebSocket() {
        try {
            client = new WebSocketClient(new URI(wsUrl)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    // 发送配置，开启语音识别
                    send(buildConfig(true));
                    readyLatch.countDown();
                    log.info("设备 {} 的 FunASR 会话已建立，配置已发送", deviceId);
                }

                @Override
                public void onMessage(String message) {
                    log.debug("设备 {} 收到 FunASR 识别结果: {}", deviceId, message);
                    if (recognitionCallback != null) {
                        try {
                            recognitionCallback.accept(message);
                        } catch (Exception e) {
                            log.error("设备 {} 识别结果回调异常", deviceId, e);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    log.warn("设备 {} 的 FunASR 连接关闭: code={}, reason={}, remote={}", deviceId, code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    isConnected = false;
                    log.error("设备 {} 的 FunASR 连接错误", deviceId, ex);
                }
            };

            if (wsUrl.startsWith("wss://")) {
                setupInsecureSSL(client);
            }

            client.connectBlocking();
            log.info("设备 {} 的 FunASR TCP 连接已建立，等待 onOpen 配置...", deviceId);

        } catch (Exception e) {
            log.error("设备 {} 连接 FunASR 失败: {}", deviceId, e.getMessage());
            isConnected = false;
        }
    }

    /**
     * 配置忽略 SSL 证书（仅开发/内网环境，生产环境应使用正式证书）
     */
    private void setupInsecureSSL(WebSocketClient wsClient) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] c, String a) {
                        }

                        public void checkServerTrusted(X509Certificate[] c, String a) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            Field field = WebSocketClient.class.getDeclaredField("socketFactory");
            field.setAccessible(true);
            field.set(wsClient, sc.getSocketFactory());
        } catch (Exception e) {
            log.debug("设备 {} SSL 配置失败: {}", deviceId, e.getMessage());
        }
    }
}