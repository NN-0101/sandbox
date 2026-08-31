#!/bin/bash

# ========================================
# 本地 Docker Registry 配置
# ========================================
REGISTRY="localhost:5000"
NAMESPACE="nn-0101"               # 模拟命名空间，与阿里云保持一致
IMAGE_NAME="sandbox"              # 仓库名

# 镜像版本标识（统一修改这里即可）
VERSION="agent-scope-latest"

# 完整镜像地址
BASE_IMAGE="${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}"
FULL_TAG="${BASE_IMAGE}:${VERSION}"

echo "========================================="
echo "Building and Pushing to Local Registry"
echo "Image: ${FULL_TAG}"
echo "========================================="

# 0. 检查本地 registry 是否运行
echo "Step 0: Checking local registry..."
if ! curl -s http://localhost:5000/v2/ > /dev/null 2>&1; then
    echo "⚠️  Local registry is not running!"
    echo "Please start it first:"
    echo "   docker-compose up -d"
    echo ""
    read -p "Have you started the registry? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Please run: docker-compose up -d"
        exit 1
    fi
fi

# 1. 构建镜像
echo "Step 1: Building image..."
docker build \
    --network=host \
    -t ${FULL_TAG} \
    .

# 2. 验证（只检查镜像是否存在，不运行容器）
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

# 3. 推送到本地仓库
echo ""
echo "Step 3: Pushing to local registry..."
echo "Pushing: ${FULL_TAG}"
docker push ${FULL_TAG}

# 4. 验证推送
echo ""
echo "Step 4: Verifying push..."
echo "Registry catalog:"
curl -s http://localhost:5000/v2/_catalog | python -m json.tool 2>/dev/null || echo "Catalog check failed"

echo ""
echo "Tags for ${NAMESPACE}/${IMAGE_NAME}:"
curl -s http://localhost:5000/v2/${NAMESPACE}/${IMAGE_NAME}/tags/list | python -m json.tool 2>/dev/null || echo "Tags check failed"

# 5. 删除本地镜像（推送成功后清理）
echo ""
echo "Step 5: Cleaning up local images..."
echo "Removing local images..."
docker rmi ${FULL_TAG} 2>/dev/null || echo "Image may still be in use, skipping..."

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