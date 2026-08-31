# 使用 Alibaba Cloud Linux 4（阿里云官方推荐，支持至2038年）
FROM alibaba-cloud-linux-4-registry.cn-hangzhou.cr.aliyuncs.com/alinux4/alinux4:latest

# 维护者信息
LABEL maintainer="nanian_0101@163.com" \
      description="Alibaba Cloud Linux 4 with JDK 17" \
      version="1.0"

# 环境变量配置
ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    TZ=Asia/Shanghai \
    JAVA_HOME=/usr/local/jdk17 \
    PATH=$PATH:/usr/local/jdk17/bin

# 配置 yum 超时时间并安装基础工具（包含 tar）（使用 --nogpgcheck 跳过 GPG 检查，解决超时问题）
RUN echo "timeout=30" >> /etc/yum.conf && \
    yum install -y --nogpgcheck \
        telnet \
        less \
        vim \
        tzdata \
        wget \
        unzip \
        tar \
        gzip \
        net-tools \
        curl \
        && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

## 安装 JDK 17 (使用 Eclipse Temurin 构建版本)
#RUN wget -q https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz -O /tmp/jdk-17.tar.gz && \
#    mkdir -p /usr/local/jdk17 && \
#    tar -xzf /tmp/jdk-17.tar.gz -C /usr/local/jdk17 --strip-components=1 && \
#    rm -f /tmp/jdk-17.tar.gz

# 复制本地的 JDK 到镜像中（假设文件名是 OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz）通过上面的链接先下载到本地，放到同级目录
COPY OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz /tmp/jdk-17.tar.gz

# 解压 JDK
RUN mkdir -p /usr/local/jdk17 && \
    tar -xzf /tmp/jdk-17.tar.gz -C /usr/local/jdk17 --strip-components=1 && \
    rm -f /tmp/jdk-17.tar.gz

# 验证JDK安装
RUN java -version

# 清理缓存
RUN yum clean all && \
    rm -rf /var/cache/yum/*

# 创建工作目录
WORKDIR /app

# 默认命令
CMD ["/bin/bash"]