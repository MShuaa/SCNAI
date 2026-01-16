# SCNAI

## 开发原则

### 一、少代码高效率（优先复用，不做过度抽象）
- 优先使用现有组件和样式，避免重复造轮子
- 保持代码简洁，避免不必要的复杂逻辑
- 页面代码量控制在100行以内，功能完整即可

### 二、注释必须为中文（所有关键改动与接口契约处）
- 所有重要的代码改动都需要中文注释说明
- 接口调用和数据处理逻辑要有清晰注释
- 临时方案和待实现功能要有明确标注

### 三、额外变更需事先同意（大型依赖或后端契约变更）
- 添加新依赖或更改项目结构需要提前讨论
- 后端接口契约变更需要双方确认
- 大型重构需要团队共识

### 四、保留并不破坏已有功能（在改动时保证现有已实现功能不中断）
- 修改代码时要确保不破坏现有功能
- 新功能开发时要标注"等待后端"而不是假设实现
- 现有页面功能必须保持稳定

### 五、风格一致性（新增页面/组件必须与已有页面风格布局一致）
- 新增页面必须复用全局布局（侧边栏 + 顶部导航）
- 配色、间距、响应式行为要与现有页面保持一致
- 不要在打开页面时隐藏或替换全局导航

## 项目结构

```
SCNAI/
├── backend/          # Spring Boot后端
├── frontend/         # Vue 3前端
├── ai_service.py     # AI服务
└── README.md         # 项目文档
```

## 快速开始

### 前端开发环境

```bash
cd frontend
npm install
npm run dev
```

开发服务器将在 `http://localhost:3000` 启动

### 开发专用Mock

前端支持开发专用Mock登录，设置环境变量启用：

```bash
# 在.env文件中添加
VITE_MOCK_AUTH=true
```

Mock账户：`demo` / `demo`

**注意：生产环境必须移除Mock代码，确保 `VITE_MOCK_AUTH` 未设置**

### 启用后端代理（联调后端时）

如果需要把前端的 `/api` 请求代理到本地后端进行联调，可以临时启用代理：

- 临时（PowerShell）:

```powershell
$env:VITE_USE_PROXY = 'true'; npm run dev
```

- 永久（写入 `.env`）:

在 `frontend/.env` 或根目录 `.env.development` 中添加：

```
VITE_USE_PROXY=true
```

然后重启开发服务器（`npm run dev`）。默认开发模式下代理为禁用，开启后会把 `/api` 请求转发到后端（`vite.config.js` 中配置的 target）。

## 技术栈

- **前端**: Vue 3 + Vite + Element Plus + Tailwind CSS
- **状态管理**: Pinia
- **后端**: Spring Boot
- **AI服务**: Python + PyTorch