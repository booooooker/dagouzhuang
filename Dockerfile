# 前端构建阶段
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --only=production
COPY frontend/ .
RUN npm run build

# 后端构建阶段
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
COPY backend/src ./src
RUN mvn clean package -DskipTests

# 最终运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 复制前端静态文件到后端资源目录
COPY --from=frontend-build /app/frontend/dist ./frontend/dist

# 复制后端 JAR 文件
COPY --from=backend-build /app/backend/target/snake-game-backend-1.0.0.jar ./backend.jar

# 创建数据目录
RUN mkdir -p /app/data

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=docker
ENV DB_PATH=/app/data/snakegame

# 启动命令
CMD ["java", "-jar", "backend.jar"]
