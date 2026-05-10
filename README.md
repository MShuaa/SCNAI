# SCNAI植鉴系统后端 - 部署指南

## 项目简介

SCNAI植鉴系统是一个基于AI的植物病虫害智能识别系统,提供以下核心功能:
1. **用户认证**: 登录/登出
2. **AI图像识别**: 上传植物图片,识别病虫害类型
3. **植物信息查询**: 查询植物详细信息
4. **识别历史记录**: 查看历史识别记录
5. **AI智能问答**: 植物病虫害相关问题的智能问答(SSE流式响应)

## 技术栈

### 后端(Java)
- Spring Boot 3.2.1
- Spring Data JPA
- MySQL 8.0
- JWT认证
- Maven

### AI服务(Python)
- PyTorch
- EfficientNet
- Flask
- PIL/Pillow

## 系统架构

```
┌─────────────┐      HTTP       ┌──────────────┐
│   前端      │ ─────────────> │  Java后端    │
│  (Vue 3)    │                 │  (Spring Boot)│
└─────────────┘                 └──────┬───────┘
                                       │
                                       │ HTTP
                                       ▼
                                ┌──────────────┐
                                │ Python AI服务│
                                │  (Flask)     │
                                └──────────────┘
                                       │
                                       │
                                       ▼
                                ┌──────────────┐
                                │  AI模型      │
                                │(EfficientNet)│
                                └──────────────┘
```

## 环境要求

### Java后端
- JDK 17或更高版本
- Maven 3.6+
- MySQL 8.0+

### Python AI服务
- Python 3.8+
- PyTorch 1.10+
- CUDA (可选,用于GPU加速)

## 安装步骤

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行数据库脚本
source database_schema.sql

# 注意: 需要手动生成admin用户的密码哈希
# 可以使用在线BCrypt工具: https://bcrypt-generator.com/
# 密码: password123
# 生成的哈希替换SQL文件中的$2a$10$PLACEHOLDER
```

### 2. 配置文件修改

编辑 `src/main/resources/application.properties`:

```properties
# 数据库配置(根据实际情况修改)
spring.datasource.url=jdbc:mysql://localhost:3306/scnai_plant...
spring.datasource.username=root
spring.datasource.password=your_password

# 文件上传路径(确保目录存在或应用有权限创建)
file.upload.path=./uploads/recognition

# AI服务地址(Python AI服务的地址)
ai.service.url=http://localhost:5001
```

### 3. Python AI服务部署

```bash
# 进入项目根目录
cd /path/to/SCNAI

# 安装Python依赖
pip install torch torchvision efficientnet-pytorch Flask Pillow numpy

# 或使用requirements.txt
pip install -r requirements.txt

# 确保模型文件存在
# 模型路径: models/best_model_g8lg426c.pth
# 如果没有模型文件,脚本会使用随机初始化的模型(仅供测试)

# 启动AI服务
python ai_service.py

# 输出:
# ==================================================
# SCNAI植物病虫害AI识别服务
# ==================================================
# 模型: EfficientNet-B0
# 类别数: 6
# 设备: cuda / cpu
# ==================================================
# API接口:
#   GET  /health - 健康检查
#   POST /predict - 单张图像识别
#   POST /predict/batch - 批量图像识别
# ==================================================
# * Running on http://0.0.0.0:5001
```

### 4. Java后端启动

```bash
# 方式1: 使用Maven
cd backend
mvn clean install
mvn spring-boot:run

# 方式2: 使用JAR包
mvn clean package
java -jar target/plant-backend-1.0.0.jar

# 启动成功后输出:
# ========================================
# SCNAI植鉴系统后端服务启动成功！
# 访问地址: http://localhost:5000/api
# ========================================
```

## API接口文档

### 基础信息
- **Base URL**: `http://localhost:5000/api`
- **认证方式**: JWT Token (除登录接口外)
- **Token格式**: `Authorization: Bearer {token}`

### 1. 用户认证

#### 登录
```
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123",
  "remember": true
}

响应:
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "系统管理员",
      "email": "admin@scnai.com"
    }
  }
}
```

#### 验证Token
```
GET /auth/verify
Authorization: Bearer {token}
```

#### 登出
```
POST /auth/logout
Authorization: Bearer {token}
```

### 2. 图像识别

```
POST /recognition
Authorization: Bearer {token}
Content-Type: multipart/form-data

参数:
- image: 图像文件(JPG/PNG, 最大3MB)
- area: 区域(可选)

响应:
{
  "success": true,
  "data": {
    "id": 123,
    "diseaseType": "powdery",
    "diseaseTypeName": "白粉病",
    "confidence": 0.9342,
    "severity": "中度",
    "identifyTime": "2024-01-16 10:30:25",
    "imageUrl": "/uploads/recognition/xxx.jpg",
    "symptoms": "叶片表面出现白色粉状物...",
    "treatmentPlan": "建议使用硫磺粉...",
    "rawPredictions": {
      "powdery": 0.9342,
      "downy": 0.0234,
      ...
    }
  }
}
```

### 3. 植物信息查询

```
GET /plants              # 获取所有植物列表
GET /plants/{id}         # 根据ID获取植物详情
GET /plants/search?name=丝瓜  # 根据名称查询植物
GET /plants/search/keyword?keyword=瓜  # 模糊搜索植物
```

### 4. 识别记录

```
GET /records?page=1&pageSize=10  # 获取识别记录列表(分页)
GET /records/{id}                # 根据ID获取记录详情
GET /records/stats               # 获取统计数据
```

### 5. AI智能问答

```
POST /chat/stream
Authorization: Bearer {token}
Content-Type: application/json

{
  "message": "丝瓜白粉病的主要症状是什么？"
}

响应: SSE流式
data: {"type":"chunk","content":"丝瓜"}
data: {"type":"chunk","content":"白粉病"}
data: {"type":"complete","fullContent":"..."}
```

## 目录结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/scnai/plant/
│   │   │   ├── PlantApplication.java      # 主应用入口
│   │   │   ├── common/
│   │   │   │   └── ApiResponse.java       # 统一响应格式
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java         # Web配置(拦截器,CORS)
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java    # 认证控制器
│   │   │   │   ├── RecognitionController.java  # 识别控制器
│   │   │   │   ├── PlantController.java   # 植物信息控制器
│   │   │   │   ├── RecordController.java  # 记录控制器
│   │   │   │   └── ChatController.java    # 聊天控制器
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java      # 登录请求DTO
│   │   │   │   ├── LoginResponse.java     # 登录响应DTO
│   │   │   │   ├── RecognitionResponse.java  # 识别响应DTO
│   │   │   │   └── ChatRequest.java       # 聊天请求DTO
│   │   │   ├── entity/
│   │   │   │   ├── User.java              # 用户实体
│   │   │   │   ├── Plant.java             # 植物实体
│   │   │   │   └── RecognitionRecord.java # 识别记录实体
│   │   │   ├── interceptor/
│   │   │   │   └── JwtInterceptor.java    # JWT拦截器
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java    # 用户数据访问
│   │   │   │   ├── PlantRepository.java   # 植物数据访问
│   │   │   │   └── RecognitionRecordRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java       # 认证服务
│   │   │   │   ├── RecognitionService.java # 识别服务
│   │   │   │   └── ChatService.java       # 聊天服务
│   │   │   └── util/
│   │   │       └── JwtUtil.java           # JWT工具类
│   │   └── resources/
│   │       └── application.properties     # 配置文件
│   └── test/
├── database_schema.sql                    # 数据库脚本
├── pom.xml                                # Maven配置
└── README.md                              # 本文件

ai_service.py                              # Python AI服务
webwork.py                                 # AI模型原始实现
models/
└── best_model_g8lg426c.pth                # AI模型文件
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 检查数据库配置(用户名/密码/数据库名)
- 确保数据库已创建: `CREATE DATABASE scnai_plant;`

### 2. Token验证失败
- 检查JWT secret配置
- 确保Token没有过期
- 检查请求头格式: `Authorization: Bearer {token}`

### 3. 文件上传失败
- 检查文件大小(最大3MB)
- 检查文件格式(仅支持JPG/PNG)
- 确保上传目录存在且有写权限

### 4. AI识别服务连接失败
- 检查Python AI服务是否启动(端口5001)
- 检查`ai.service.url`配置
- 检查防火墙设置

### 5. Python AI服务启动失败
- 检查Python版本(需要3.8+)
- 安装所有依赖: `pip install -r requirements.txt`
- 检查模型文件路径
- 如果没有GPU,会自动使用CPU(速度较慢)

## 测试账号

```
用户名: admin
密码: password123
角色: 系统管理员
```

## 性能优化建议

1. **数据库索引**: 已在识别记录表添加必要索引
2. **文件存储**: 生产环境建议使用云存储(OSS/S3)
3. **AI服务**: 建议使用GPU加速推理
4. **缓存**: 可添加Redis缓存热点数据
5. **负载均衡**: 生产环境可部署多个AI服务实例

## 开发建议

### 集成真实LLM服务
当前智能问答使用模拟实现,可集成:
- OpenAI GPT-3.5/4
- DeepSeek
- 本地部署的开源LLM(LLaMA, Qwen等)

修改`ChatService.java`的`streamChat`方法即可。

### 集成真实天气API
可集成第三方天气服务:
- OpenWeatherMap
- 和风天气
- 中国天气网

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题,请联系开发团队。
