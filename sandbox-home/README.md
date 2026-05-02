# sandbox-home

> 用语音对话你的智能家园 🏠

sandbox-home 是 Sandbox 项目的首个实验模块，实现从语音识别到 AI 对话再到设备控制的完整闭环。基于 Spring AI + Netty + FunASR 技术栈，打造完全本地化的智能家居中控平台。

**注意**：本模块为单体服务，不依赖任何外部 common 包，所有代码自包含。后续模块会抽取公共部分到 sandbox-common。

---

## 🎯 核心功能

### 1. 🎙️ 语音交互
- **实时语音识别**：设备音频流 → Netty → FunASR 引擎 → 识别文本
- **2pass 模式**：实时片段(online) + 离线校验(offline)双通道，兼顾速度与准度
- **会话管理**：一设备一会话，自动创建与清理，支持并发多设备

### 2. 🤖 AI 对话
- **多业务场景**：设备对话、用户闲聊，策略模式灵活切换
- **流式响应**：基于 Reactor Flux 的流式输出，逐字返回体验
- **多轮记忆**：数据库持久化对话历史（DBChatMemory），支持上下文理解
- **向量检索**：Redis Vector Store 集成，支持 RAG 检索增强生成
- **MCP 协议**：Model Context Protocol 客户端集成

### 3. 📡 设备通信
- **长连接管理**：Netty WebSocket，设备认证、心跳保活
- **责任链处理**：Frame → Conn → Heartbeat → Audio → Default
- **全局路由**：DeviceChannelGroup 统一管理所有设备连接
- **流式下发**：AI 回复逐片段推送设备端

### 4. 🗄️ 数据架构
- **读写分离**：ShardingSphere 主从数据源，读从库写主库
- **字段加密**：ShardingSphere 加密规则，敏感字段 AES 加密
- **分布式 ID**：雪花算法，WorkerId 基于 IP 自动生成

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 | 用途 |
| :--- | :--- | :--- | :--- |
| 核心框架 | Spring Boot | 3.5.14 | 应用框架 |
| AI 框架 | Spring AI | 1.1.5 | ChatClient / Memory / VectorStore / MCP |
| 微服务 | Spring Cloud Alibaba | 2023.0.3.4 | Nacos 服务注册发现 + 配置中心 |
| 网络通信 | Netty | 4.1.116 | 设备 WebSocket 服务器 |
| WebSocket 客户端 | Java-WebSocket | 1.5.4 | FunASR 通信客户端 |
| ORM | MyBatis-Plus | 3.5.5 | 批量插入 / 分页 / Lambda 条件构造 |
| 数据库中间件 | ShardingSphere | 5.0.0 | 读写分离 + 字段加密 |
| 连接池 | Druid | 1.2.27 | 监控 / 慢 SQL / 防火墙 |
| 缓存/向量 | Redis + Jedis | - | 缓存 + Redis Vector Store (RAG) |
| 数据库 | MySQL | 8.0.28 | 主数据库 |
| 语音识别 | FunASR | 最新 | 离线语音识别引擎 |
| JSON | Fastjson2 | 2.0.53 | 序列化/反序列化 |
| 工具 | Hutool | 5.8.26 | 通用工具 |
| 工具 | Lombok | 1.18.20 | 简化代码 |
| 工具 | Commons Lang3 | 3.12.0 | 字符串/对象工具 |
| 日志链路 | TLog | 1.5.2 | 链路追踪日志 |
