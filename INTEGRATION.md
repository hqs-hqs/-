# Vue前端与Java后端集成指南

本文档说明如何将Vue前端项目与Java Spring Boot后端连接起来。

## 项目结构

```
项目根目录/
├── src/                    # Vue前端代码
│   ├── api/               # API服务封装
│   │   └── index.js      # axios配置和API方法
│   └── components/        # Vue组件
├── backend/               # Java后端代码
│   ├── src/main/java/    # Java源代码
│   └── pom.xml           # Maven依赖
└── vite.config.js         # Vite配置（已添加代理）
```

## 快速开始

### 方式一：使用API模式（推荐）

#### 1. 启动Java后端

```bash
cd backend
# 确保已安装Java 17+ 和 Maven
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

#### 2. 配置前端使用API

创建 `.env` 文件（或修改现有配置）：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_USE_API=true
```

#### 3. 启动Vue前端

```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 方式二：使用localStorage模式（原有方式）

如果不想使用后端，可以继续使用localStorage：

```env
VITE_USE_API=false
```

或者不设置 `VITE_USE_API`，前端会自动使用localStorage模式。

## 配置说明

### 前端配置

1. **API基础URL**：在 `.env` 文件中设置 `VITE_API_BASE_URL`
2. **API模式开关**：设置 `VITE_USE_API=true` 启用API模式
3. **代理配置**：`vite.config.js` 中已配置代理，可直接使用 `/api` 路径

### 后端配置

1. **数据库配置**：修改 `backend/src/main/resources/application.properties`
   - MySQL：配置数据库连接信息
   - H2（开发）：使用 `application-dev.properties`

2. **跨域配置**：`CorsConfig.java` 已配置允许所有来源（开发环境）
   - 生产环境应限制为具体域名

## API接口说明

### 用户相关

- `POST /api/user/register` - 注册
  ```json
  {
    "username": "test",
    "password": "123456"
  }
  ```

- `POST /api/user/login` - 登录
  ```json
  {
    "username": "test",
    "password": "123456"
  }
  ```

- `POST /api/user/reset-password` - 重置密码
  ```json
  {
    "username": "test",
    "newPassword": "newpass"
  }
  ```

### 响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "token": "jwt_token_here"  // 登录时返回
}
```

## 数据库设置

### 使用MySQL

1. 创建数据库：
```sql
CREATE DATABASE game_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.properties` 中的数据库连接信息

3. Spring Boot会自动创建表结构（`spring.jpa.hibernate.ddl-auto=update`）

### 使用H2（开发环境）

1. 使用 `application-dev.properties` 配置
2. 无需手动创建数据库，H2会自动创建内存数据库

## 开发建议

### 1. 逐步迁移

可以逐步将功能从localStorage迁移到API：
- 先迁移用户登录/注册
- 再迁移游戏数据
- 最后迁移排行榜和商城

### 2. 错误处理

前端已添加错误处理，API调用失败时会显示错误信息。

### 3. Token认证

当前使用简单的mock token，生产环境应：
- 使用JWT生成真实token
- 添加token刷新机制
- 实现token过期处理

### 4. 数据同步

考虑添加：
- 离线数据缓存
- 数据同步机制
- 冲突解决策略

## 常见问题

### 1. 跨域错误

确保后端 `CorsConfig.java` 配置正确，允许前端域名访问。

### 2. 连接失败

- 检查后端是否启动
- 检查 `VITE_API_BASE_URL` 配置是否正确
- 检查防火墙设置

### 3. 数据库连接失败

- 检查数据库是否启动
- 检查数据库用户名密码
- 检查数据库驱动是否正确

## 下一步

1. **完善API接口**：添加游戏数据、排行榜、商城等接口
2. **实现JWT认证**：替换mock token为真实JWT
3. **添加数据验证**：前后端都添加输入验证
4. **优化性能**：添加缓存、分页等
5. **部署**：配置生产环境部署

## 参考文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [Axios文档](https://axios-http.com/)

