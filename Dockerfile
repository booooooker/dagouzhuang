FROM node:20-alpine AS frontend
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
COPY --from=frontend /fe/dist ./src/main/resources/static
RUN mvn -B package -DskipTests
RUN cp /app/target/snake-game-backend-*.jar /app/application.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/application.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
