# Java后端启动指南

## 前置条件

1. **Java 17+** 
   - 检查：`java -version`
   - 下载：https://www.oracle.com/java/technologies/downloads/

2. **Maven 3.6+**（IDEA通常自带）
   - 检查：`mvn -version`
   - 下载：https://maven.apache.org/download.cgi

3. **IntelliJ IDEA**（推荐）或其他Java IDE

## 方式一：在IDEA中启动（推荐）

### 步骤1：导入项目

1. 打开IDEA
2. 选择 `File` → `Open`
3. 选择 `backend` 文件夹
4. 等待Maven自动下载依赖（右下角会显示进度）

### 步骤2：配置JDK

1. `File` → `Project Structure` (Ctrl+Alt+Shift+S)
2. 在 `Project` 标签页：
   - `SDK`: 选择 Java 17 或更高版本
   - `Language level`: 选择 17 或更高

### 步骤3：配置运行环境（使用H2数据库，无需MySQL）

1. 在IDEA中，找到 `src/main/resources/application.properties`
2. 暂时注释掉MySQL配置，使用H2内存数据库（开发环境）

或者直接使用 `application-dev.properties`（已配置好H2）

### 步骤4：启动项目

**方法A：直接运行主类**
1. 找到 `src/main/java/com/game/GameApplication.java`
2. 右键点击文件
3. 选择 `Run 'GameApplication'` 或点击左侧绿色三角形

**方法B：使用Maven命令**
1. 打开IDEA底部的 `Terminal`
2. 输入：`mvn spring-boot:run`

### 步骤5：验证启动

看到以下日志表示启动成功：
```
Started GameApplication in X.XXX seconds
```

访问：http://localhost:8080 （应该会显示404或错误页面，这是正常的）

## 方式二：命令行启动

### Windows (PowerShell/CMD)

```bash
cd backend
mvn spring-boot:run
```

### Mac/Linux

```bash
cd backend
./mvnw spring-boot:run
```

## 数据库配置

### 选项1：使用H2内存数据库（最简单，推荐开发环境）

无需安装任何数据库，直接使用 `application-dev.properties`：

1. 在IDEA中，编辑运行配置：
   - `Run` → `Edit Configurations`
   - 找到 `GameApplication`
   - 在 `Active profiles` 填入：`dev`
   - 或者在 `VM options` 填入：`-Dspring.profiles.active=dev`

2. 或者修改 `application.properties`，临时使用H2：
```properties
# 注释掉MySQL配置，使用H2
# spring.datasource.url=jdbc:mysql://...
spring.datasource.url=jdbc:h2:mem:gamedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### 选项2：使用MySQL（生产环境）

1. 安装MySQL
2. 创建数据库：
```sql
CREATE DATABASE game_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改 `application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
```

## 常见问题

### 1. 端口被占用

如果8080端口被占用，修改 `application.properties`：
```properties
server.port=8081
```

### 2. Maven依赖下载失败

- 检查网络连接
- 配置Maven镜像（国内推荐阿里云镜像）
- 在IDEA中：`File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`

### 3. Java版本不匹配

确保使用Java 17或更高版本：
- IDEA中：`File` → `Project Structure` → `Project` → `SDK`

### 4. Spring Security阻止访问

如果遇到401/403错误，需要配置Spring Security允许所有请求（开发环境）。

## 测试API

启动成功后，可以使用以下方式测试：

### 使用浏览器
访问：http://localhost:8080/api/user/current

### 使用Postman或curl
```bash
# 注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'
```

## 下一步

1. 确保后端启动成功（端口8080）
2. 配置前端使用API（设置 `.env` 文件）
3. 启动前端：`npm run dev`
4. 测试登录功能

