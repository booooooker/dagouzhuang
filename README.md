# 吃狗庄 - 股票贪吃蛇游戏

一个融合股票交易术语的贪吃蛇游戏，支持积分系统和排行榜。

## 项目结构

```
dagouzhuang/
├── backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/snakegame/
│           │   ├── SnakeGameApplication.java
│           │   ├── WebMvcConfig.java
│           │   ├── controller/
│           │   │   └── GameController.java
│           │   ├── entity/
│           │   │   └── User.java
│           │   ├── repository/
│           │   │   └── UserRepository.java
│           │   └── service/
│           │       └── UserService.java
│           └── resources/
│               └── application.yml
├── frontend/                # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── admin.html
│   └── src/
│       ├── main.js
│       ├── api.js
│       ├── App.vue
│       └── Admin.vue
├── data/                    # 数据库文件目录
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## 技术栈

- **后端**: Spring Boot 3.2 + JPA + H2数据库
- **前端**: Vue 3 + Vite + Axios
- **数据库**: H2 (文件模式，数据持久化)
- **容器**: Docker + Docker Compose

## 运行项目

### 方式一：使用 Docker Compose（推荐）

```bash
# 构建并启动容器
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

服务启动后访问:
- 游戏首页: http://localhost:8080
- 管理后台: http://localhost:8080/admin.html

### 方式二：本地开发运行

#### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端会在 http://localhost:8080 启动

#### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端会在 http://localhost:3000 启动

## 游戏规则

- 🐕 **吃狗** = 建仓（+10分）
- 🐕 **吃狗庄** = 平仓（+50分）
- 💣 **撞炸弹** = 爆仓（游戏结束）
- ⚠️ **撞障碍物** = 爆仓（游戏结束）
- 📉 **撞墙** = 割肉（游戏结束）
- 📈 **撞自己** = 止损（游戏结束）

## 功能特性

1. **自动用户创建**: 根据访问IP自动创建用户
2. **积分系统**: 累计积分，以美元显示
3. **排行榜**: 支持按总积分和最高分排序
4. **股票术语**: 游戏元素融合了牛市/熊市、建仓、平仓等概念
5. **双模式切换**: 游戏中会自动切换牛市(蓝绿色调)和熊市(红黄色调)
6. **障碍物系统**: 游戏中有炸弹和障碍物增加难度
7. **管理后台**: 管理员可查看所有用户数据

## 管理后台

访问地址: `/admin.html`

登录密码: `buge2026`

功能:
- 查看所有用户列表
- 用户搜索（昵称/IP）
- 用户删除
- 统计数据展示

## API接口

### 用户接口
- `GET /api/user` - 获取当前用户
- `PUT /api/user/nickname?userId=&nickname=` - 修改昵称

### 游戏接口
- `POST /api/game/end?userId=&score=&isWin=` - 提交游戏结果

### 排行榜接口
- `GET /api/leaderboard/score` - 总积分榜
- `GET /api/leaderboard/maxscore` - 最高分榜

### 管理员接口
- `POST /api/admin/login?password=` - 管理员登录
- `GET /api/admin/users` - 获取所有用户
- `DELETE /api/admin/user/{userId}` - 删除用户

## Docker 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | docker | Spring 配置文件 |
| `DB_PATH` | /app/data/snakegame | 数据库文件路径 |

## 数据持久化

使用 Docker Compose 运行时，数据会自动持久化到 `./data` 目录。

## License

MIT
