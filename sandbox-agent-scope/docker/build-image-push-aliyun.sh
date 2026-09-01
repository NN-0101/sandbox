#!/bin/bash

# ========================================
# 阿里云容器镜像服务配置
# ========================================
REGISTRY="registry.cn-hangzhou.aliyuncs.com"
NAMESPACE="nn-0101"
IMAGE_NAME="sandbox"              # 仓库名

# 镜像版本标识（统一修改这里即可）
VERSION="agent-scope-latest"

# 应用配置
APP_NAME="sandbox-agent-scope"
JAR_VERSION="1.0.0"
JAR_FILE="${APP_NAME}-${JAR_VERSION}.jar"

# 完整镜像地址
BASE_IMAGE="${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}"
FULL_TAG="${BASE_IMAGE}:${VERSION}"

echo "========================================="
echo "Building and Pushing to Aliyun Registry"
echo "Image: ${FULL_TAG}"
echo "========================================="

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "Script directory: ${SCRIPT_DIR}"
echo "Project root: ${PROJECT_ROOT}"

# 定义清理函数（确保在任何退出时清理）
cleanup() {
    local jar_path="${SCRIPT_DIR}/${JAR_FILE}"
    if [ -f "${jar_path}" ]; then
        echo ""
        echo "🧹 Cleaning up temporary JAR file..."
        rm -f "${jar_path}"
        echo "✅ Temporary JAR removed: ${jar_path}"
    fi
}

# 设置 trap，确保脚本退出时执行清理（包括异常退出）
trap cleanup EXIT

# 0. 检查并准备 JAR 包
echo ""
echo "Step 0: Preparing JAR file..."

JAR_SOURCE="${PROJECT_ROOT}/target/${JAR_FILE}"
JAR_TARGET="${SCRIPT_DIR}/${JAR_FILE}"

# 检查 docker 目录是否已有 JAR（如果有，先删除避免使用旧版本）
if [ -f "${JAR_TARGET}" ]; then
    echo "Removing existing JAR in docker directory..."
    rm -f "${JAR_TARGET}"
fi

# 检查 target 目录是否有 JAR
if [ ! -f "${JAR_SOURCE}" ]; then
    echo "⚠️  JAR not found in target directory: ${JAR_SOURCE}"
    echo ""
    echo "Please build the project first:"
    echo "  cd ${PROJECT_ROOT}"
    echo "  mvn clean package -DskipTests"
    echo ""
    read -p "Do you want to build the project now? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Building project..."
        cd "${PROJECT_ROOT}"
        mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "❌ Build failed!"
            exit 1
        fi
        cd "${SCRIPT_DIR}"
    else
        echo "❌ Cannot proceed without JAR file"
        exit 1
    fi
fi

# 复制 JAR 到 docker 目录
echo "Copying JAR from ${JAR_SOURCE}"
echo "          to ${JAR_TARGET}"
cp "${JAR_SOURCE}" "${JAR_TARGET}"
if [ $? -eq 0 ]; then
    echo "✅ JAR copied successfully"
else
    echo "❌ Failed to copy JAR"
    exit 1
fi

# 检查 entrypoint.sh 是否存在
ENTRYPOINT_SOURCE="${PROJECT_ROOT}/entrypoint.sh"
ENTRYPOINT_TARGET="${SCRIPT_DIR}/entrypoint.sh"
if [ -f "${ENTRYPOINT_SOURCE}" ] && [ ! -f "${ENTRYPOINT_TARGET}" ]; then
    echo "Copying entrypoint.sh to docker directory..."
    cp "${ENTRYPOINT_SOURCE}" "${ENTRYPOINT_TARGET}"
fi

# 0.1 检查是否已登录
echo ""
echo "Step 0.1: Checking login status..."
if ! docker system info 2>/dev/null | grep -q "${REGISTRY}"; then
    echo "⚠️  Please login first:"
    echo "   docker login --username=NN-0101 ${REGISTRY}"
    echo ""
    read -p "Have you logged in? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Please run: docker login ${REGISTRY}"
        exit 1
    fi
fi

# 1. 构建镜像（在 docker 目录下构建）
echo ""
echo "Step 1: Building image..."
cd "${SCRIPT_DIR}"
docker build \
    --network=host \
    -t ${FULL_TAG} \
    .

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "❌ Docker build failed!"
    exit 1
fi

# 2. 验证（只检查镜像是否存在，不运行容器）
echo ""
echo "Step 2: Verifying image..."
if docker image inspect ${FULL_TAG} > /dev/null 2>&1; then
    echo "✅ Image built successfully: ${FULL_TAG}"
    echo ""
    echo "Image details:"
    docker images ${BASE_IMAGE} --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
else
    echo "❌ Image not found!"
    exit 1
fi

# 3. 推送到阿里云
echo ""
echo "Step 3: Pushing to Aliyun registry..."
echo "Pushing: ${FULL_TAG}"
docker push ${FULL_TAG}

# 4. 验证推送
echo ""
echo "Step 4: Verifying push..."
echo "You can check your images at:"
echo "https://cr.console.aliyun.com/"
echo ""
echo "Or use API to check:"
curl -s -X GET "https://${REGISTRY}/v2/${NAMESPACE}/${IMAGE_NAME}/tags/list" | python -m json.tool 2>/dev/null || echo "API check may require authentication"

# 5. 删除本地镜像（推送成功后清理）
echo ""
echo "Step 5: Cleaning up local images..."
echo "Removing local images..."
docker rmi ${FULL_TAG} 2>/dev/null || echo "Image may still be in use, skipping..."

# 6. 清理临时 JAR 文件（由 trap 自动执行）
echo ""
echo "Step 6: Temporary JAR will be cleaned up automatically..."

echo ""
echo "========================================="
echo "✅ Build, push, and cleanup completed successfully!"
echo "========================================="
echo ""
echo "To pull the image:"
echo "  docker pull ${FULL_TAG}"
echo ""
echo "To use in Docker Compose:"
echo "  image: ${FULL_TAG}"