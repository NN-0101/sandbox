# FROM localhost:5000/nn-0101/sandbox:alinux4-jdk17
FROM registry.cn-hangzhou.aliyuncs.com/nn-0101/sandbox:alinux4-jdk17

# 声明构建参数
ARG APP_NAME=sandbox-agent-scope

WORKDIR /apprun

# 环境变量配置
ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    TZ=Asia/Shanghai \
    PROFILES_ACTIVE=prod \
    APP_NAME=sandbox-agent-scope \
    JAR=${APP_NAME}-1.0.0.jar \
    JVM="-Xlog:gc*:file=/apprun/logs/${APP_NAME}/gc.log:time,uptimemillis:filecount=5,filesize=100m \
        -XX:+UseG1GC \
        -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=/apprun/logs/${APP_NAME}/ \
        -Xms4096m -Xmx4096m"

# 添加应用文件
ADD *.jar /apprun/${APP_NAME}-1.0.0.jar
ADD entrypoint.sh /apprun/

# 基础镜像已是 Alinux4，已配置好阿里云源，只需创建目录和设置权限
RUN mkdir -p /apprun/logs/${APP_NAME} && \
    chmod +x /apprun/entrypoint.sh

# 容器启动命令
ENTRYPOINT ["/bin/sh", "/apprun/entrypoint.sh"]