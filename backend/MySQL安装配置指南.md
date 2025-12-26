# MySQL 安装和配置指南

## 📋 快速理解

**简单来说：**
- **MySQL Installer** = 安装 MySQL 数据库服务器（必须）
- **MySQL Workbench** = 图形化管理工具（推荐，方便查看数据）

**好消息：** MySQL Installer 通常已经包含了 Workbench，**只需要下载一个安装程序**！

**安装建议：** 选择 "Developer Default" 安装类型，会自动安装服务器 + Workbench。

---

## 一、关于 SQL Server 残留问题

**好消息**：SQL Server 的残留通常**不会影响** MySQL 的安装，因为：
- SQL Server 默认使用端口 **1433**
- MySQL 默认使用端口 **3306**
- 它们使用不同的注册表项和服务

如果担心冲突，可以在安装 MySQL 时选择不同的端口（但建议使用默认的 3306）。

---

## 二、两个工具的区别

### 📦 MySQL Installer（MySQL 安装程序）
**作用**：安装 **MySQL 服务器**本身
- 这是数据库的核心程序，负责存储和管理数据
- 安装后会在后台运行一个服务（MySQL80）
- 你的项目会连接到这个服务器来读写数据
- **必须安装**，否则项目无法连接数据库

### 🛠️ MySQL Workbench（MySQL 工作台）
**作用**：MySQL 的**图形化管理工具**
- 用来可视化查看、管理数据库
- 可以查看表、数据、执行 SQL 语句
- 方便调试和查看数据
- **可选安装**，但强烈推荐（比命令行方便很多）

### 💡 重要提示
**好消息**：MySQL Installer 通常**已经包含了 MySQL Workbench**！
- 当你选择 "Developer Default" 安装类型时，Workbench 会自动包含
- **所以通常只需要下载一个 MySQL Installer 就够了**

---

## 三、安装步骤（推荐方式）

### 方法1：使用 MySQL Installer（推荐，一次安装两个）

1. **下载 MySQL Installer**
   - 访问：https://dev.mysql.com/downloads/installer/
   - 选择 **MySQL Installer for Windows**
   - 下载 **mysql-installer-community-8.0.xx.x.x.msi**（约 400MB）

2. **安装步骤**
   - 运行安装程序
   - **选择 "Developer Default"**（开发默认）- 这个选项会自动包含 MySQL Workbench
   - 点击 **Execute** 安装所有组件
   - 等待安装完成（会安装 MySQL Server + Workbench + 其他工具）

3. **配置 MySQL Server**
   - 选择 **Standalone MySQL Server / Classic MySQL Replication**
   - 配置类型选择 **Development Computer**（开发计算机）
   - 端口保持默认 **3306**
   - 设置 **root 密码**（请记住这个密码！）
   - Windows Service 名称保持默认 **MySQL80**
   - 点击 **Execute** 完成配置

4. **验证安装**
   - 打开命令提示符（CMD）
   - 输入：`mysql --version`
   - 应该显示 MySQL 版本信息
   - 打开开始菜单，应该能看到 **MySQL Workbench** 图标

### 方法2：只安装 MySQL Server（如果不需要图形界面）

1. 使用 MySQL Installer
2. 选择 **Server only**（仅服务器）
3. 这样只会安装 MySQL 服务器，不包含 Workbench
4. 需要用命令行操作数据库（不推荐，不方便）

### 方法3：单独安装 Workbench（如果已经安装了 MySQL Server）

如果你之前已经安装了 MySQL Server，只需要图形工具：
- 访问：https://dev.mysql.com/downloads/workbench/
- 单独下载并安装 MySQL Workbench

---

## 四、配置 MySQL Workbench 连接

### 1. 创建数据库连接

1. 打开 **MySQL Workbench**
2. 点击 **MySQL Connections** 旁边的 **+** 按钮
3. 填写连接信息：
   - **Connection Name**: `本地游戏数据库`（任意名称）
   - **Hostname**: `localhost` 或 `127.0.0.1`
   - **Port**: `3306`
   - **Username**: `root`
   - **Password**: 点击 **Store in Keychain** 或 **Store in Vault**，输入你设置的 root 密码
4. 点击 **Test Connection** 测试连接
5. 如果成功，点击 **OK** 保存

### 2. 创建项目数据库

1. 双击刚才创建的连接，输入密码连接
2. 在左侧导航栏右键点击，选择 **Create Schema...**
3. 数据库名称输入：`game_db`
4. 字符集选择：`utf8mb4`
5. 排序规则选择：`utf8mb4_unicode_ci`
6. 点击 **Apply** 创建数据库

---

## 五、配置项目连接

### 1. 修改 application.properties

编辑 `backend/src/main/resources/application.properties`：

```properties
# 数据库配置 - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的MySQL密码  # 修改这里！
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver  # 注意：8.0版本使用cj
```

### 2. 修改驱动类名（重要！）

MySQL 8.0 应该使用 `com.mysql.cj.jdbc.Driver`，而不是 `com.mysql.jdbc.Driver`。

如果 `application.properties` 中使用的是旧驱动，请修改为：
```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## 六、启动项目并验证

1. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **检查日志**
   - 如果看到 "HikariPool-1 - Starting..." 和 "HikariPool-1 - Start completed"，说明连接成功
   - 如果看到错误，检查密码和数据库名称是否正确

3. **在 MySQL Workbench 中查看**
   - 刷新左侧导航栏
   - 展开 `game_db` 数据库
   - 应该能看到自动创建的表（如 `user`, `rank`, `game_data` 等）

---

## 七、常见问题

### 问题1：端口 3306 被占用

**解决方案**：
- 检查是否有其他 MySQL 实例在运行
- 打开任务管理器，结束 `mysqld.exe` 进程
- 或者修改 MySQL 端口（不推荐）

### 问题2：无法连接到 MySQL

**检查清单**：
- ✅ MySQL 服务是否启动（服务名称：MySQL80）
- ✅ 端口 3306 是否开放
- ✅ root 密码是否正确
- ✅ 防火墙是否阻止连接

**启动 MySQL 服务**：
- 按 `Win + R`，输入 `services.msc`
- 找到 **MySQL80** 服务
- 右键选择 **启动**

### 问题3：SQL Server 残留影响

**如果确实有冲突**：
1. 完全卸载 SQL Server（使用官方卸载工具）
2. 清理注册表（谨慎操作）
3. 重启电脑后再安装 MySQL

**但通常不需要**，因为端口不同。

### 问题4：忘记 root 密码

**重置密码步骤**：
1. 停止 MySQL 服务
2. 以管理员身份运行 CMD
3. 使用 `--skip-grant-tables` 启动 MySQL
4. 连接并修改密码
5. 重启 MySQL 服务

详细步骤请参考：https://dev.mysql.com/doc/refman/8.0/en/resetting-permissions.html

---

## 八、快速验证脚本

在 MySQL Workbench 中执行以下 SQL 验证数据库：

```sql
-- 查看所有数据库
SHOW DATABASES;

-- 使用 game_db 数据库
USE game_db;

-- 查看所有表
SHOW TABLES;

-- 查看用户表结构
DESCRIBE user;

-- 查看排行榜表结构
DESCRIBE rank;
```

---

## 九、推荐配置

### 开发环境
- 使用 **H2 内存数据库**（当前配置）
- 无需安装 MySQL
- 数据在应用重启后丢失

### 生产环境
- 使用 **MySQL 8.0**
- 持久化存储数据
- 需要安装和配置 MySQL

---

## 十、下一步

安装完成后：
1. ✅ 创建数据库连接
2. ✅ 创建 `game_db` 数据库
3. ✅ 修改 `application.properties` 中的密码
4. ✅ 启动后端项目
5. ✅ 在 Workbench 中查看自动创建的表

如有问题，请检查：
- MySQL 服务是否运行
- 密码是否正确
- 端口是否被占用
- 防火墙设置

