# Java后端项目

这是一个Spring Boot后端项目，为Vue前端游戏项目提供API服务。

## 技术栈

- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- MySQL / H2 Database（开发环境可用H2）
- Spring Security（可选，用于JWT认证）

## 项目结构

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── game/
│       │           ├── GameApplication.java          # 启动类
│       │           ├── config/
│       │           │   ├── CorsConfig.java          # 跨域配置
│       │           │   └── WebConfig.java           # Web配置
│       │           ├── controller/
│       │           │   ├── UserController.java      # 用户控制器
│       │           │   ├── GameController.java      # 游戏数据控制器
│       │           │   ├── RankController.java      # 排行榜控制器
│       │           │   └── ShopController.java     # 商城控制器
│       │           ├── service/
│       │           │   ├── UserService.java
│       │           │   ├── GameService.java
│       │           │   ├── RankService.java
│       │           │   └── ShopService.java
│       │           ├── repository/
│       │           │   ├── UserRepository.java
│       │           │   ├── GameDataRepository.java
│       │           │   ├── RankRepository.java
│       │           │   └── ShopRepository.java
│       │           ├── entity/
│       │           │   ├── User.java
│       │           │   ├── GameData.java
│       │           │   ├── RankRecord.java
│       │           │   └── ShopItem.java
│       │           └── dto/
│       │               ├── LoginRequest.java
│       │               ├── RegisterRequest.java
│       │               └── ApiResponse.java
│       └── resources/
│           ├── application.properties              # 配置文件
│           └── application-dev.properties          # 开发环境配置
└── pom.xml                                          # Maven依赖
```

## 快速开始

### 1. 创建Spring Boot项目

使用Spring Initializr创建项目：
- 访问 https://start.spring.io/
- 选择：
  - Project: Maven
  - Language: Java
  - Spring Boot: 3.2.x
  - Dependencies: 
    - Spring Web
    - Spring Data JPA
    - MySQL Driver (或H2 Database用于开发)
    - Lombok (可选，简化代码)

### 2. 配置文件

在 `application.properties` 中配置：

```properties
# 服务器端口
server.port=8080

# 数据库配置（MySQL）
spring.datasource.url=jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 跨域配置（开发环境）
cors.allowed-origins=http://localhost:5173,http://localhost:5174
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

或使用IDE直接运行 `GameApplication.java`

## API接口说明

### 用户相关

- `POST /api/user/register` - 注册
- `POST /api/user/login` - 登录
- `POST /api/user/reset-password` - 重置密码
- `GET /api/user/current` - 获取当前用户

### 游戏数据

- `GET /api/game/user/{userId}` - 获取用户游戏数据
- `POST /api/game/user/{userId}` - 保存用户游戏数据
- `PUT /api/game/user/{userId}` - 更新用户游戏数据

### 排行榜

- `GET /api/rank/{gameType}?mode=xxx` - 获取排行榜
- `POST /api/rank/{gameType}` - 提交排行榜记录

### 商城

- `GET /api/shop/items?category=xxx` - 获取商品列表
- `GET /api/shop/user/{userId}/skins` - 获取用户拥有的皮肤
- `POST /api/shop/purchase` - 购买商品
- `GET /api/shop/user/{userId}/coins` - 获取用户金币
- `PUT /api/shop/user/{userId}/coins` - 更新用户金币

## 数据库设计

### User表
- id (主键)
- username (用户名，唯一)
- password (密码，加密存储)
- coins (金币)
- created_at
- updated_at

### GameData表
- id (主键)
- user_id (外键)
- game_type (游戏类型)
- data (JSON格式的游戏数据)
- created_at
- updated_at

### RankRecord表
- id (主键)
- user_id (外键)
- game_type (游戏类型)
- mode (模式)
- score (分数)
- time (时间)
- rank_data (JSON格式的排行榜数据)
- created_at

### ShopItem表
- id (主键)
- item_id (商品ID)
- name (商品名称)
- category (分类)
- price (价格)
- emoji (图标)

### UserSkin表
- id (主键)
- user_id (外键)
- item_id (商品ID)
- created_at

## 注意事项

1. **跨域配置**：确保后端允许前端域名访问
2. **密码加密**：使用BCrypt等加密算法存储密码
3. **JWT认证**：生产环境建议使用JWT token进行身份认证
4. **数据验证**：添加输入验证和异常处理
5. **数据库索引**：为常用查询字段添加索引

