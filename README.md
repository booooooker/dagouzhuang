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
└── frontend/                # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── api.js
        └── App.vue
```

## 技术栈

- **后端**: Spring Boot 3.2 + JPA + H2数据库
- **前端**: Vue 3 + Vite + Axios
- **数据库**: H2 (内存/文件混合模式)

## 运行项目

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端会在 http://localhost:8080 启动

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端会在 http://localhost:3000 启动

## 游戏规则

- 🎯 **吃金色食物** = 建仓（+10分）
- 🐕 **吃狗庄** = 平仓（+50分）
- 📉 **撞墙** = 割肉（游戏结束）
- 📈 **撞自己** = 止损（游戏结束）

## 功能特性

1. **自动用户创建**: 根据访问IP自动创建用户
2. **积分系统**: 累计积分，总积分榜
3. **排行榜**: 支持按总积分和最高分排序
4. **股票术语**: 游戏元素融合了牛市/熊市、建仓、平仓等概念
5. **双模式切换**: 游戏中会随机切换牛市(蓝绿色调)和熊市(红黄色调)

## API接口

- `GET /api/user` - 获取当前用户
- `POST /api/game/end?userId=&score=&isWin=` - 提交游戏结果
- `PUT /api/user/nickname?userId=&nickname=` - 修改昵称
- `GET /api/leaderboard/score` - 总积分榜
- `GET /api/leaderboard/maxscore` - 最高分榜
