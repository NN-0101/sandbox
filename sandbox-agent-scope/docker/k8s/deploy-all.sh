#!/bin/bash

echo "========================================="
echo "部署 Sandbox Agent 到 Kubernetes"
echo "========================================="

# 1. 创建 Secret
echo ""
echo "Step 1: 创建 Secret..."
kubectl apply -f secret.yaml

# 2. 部署应用
echo ""
echo "Step 2: 部署应用..."
kubectl apply -f deployment.yaml

# 3. 等待 Pod 就绪
echo ""
echo "Step 3: 等待 Pod 启动..."
sleep 5

# 4. 查看状态
echo ""
echo "Step 4: 查看状态..."
kubectl get pods
kubectl get svc
kubectl get endpoints

# 5. 显示访问地址
echo ""
echo "========================================="
echo "✅ 部署完成！"
echo ""
echo "访问地址:"
echo "  http://localhost:30113/sandbox-agent-scope/quick-start/build-by-builder"
echo ""
echo "查看日志:"
echo "  kubectl logs -f deployment/sandbox-agent"
echo "========================================="