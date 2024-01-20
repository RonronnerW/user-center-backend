# 使用官方的 Maven 镜像作为构建阶段的基础镜像
FROM maven:3.8.4-openjdk-17 AS build

# 设置工作目录
WORKDIR /app

# 复制 Maven 项目描述文件
COPY pom.xml .

# 下载依赖项并在构建阶段缓存它们
RUN mvn dependency:go-offline

# 复制整个项目
COPY src src

# 执行 Maven 构建
RUN mvn package -DskipTests

# 使用官方的 OpenJDK 17 镜像作为运行阶段的基础镜像
# FROM openjdk:17-alpine

# 设置工作目录
# WORKDIR /app

# 从构建阶段复制构建好的 JAR 文件
# COPY --from=build /app/user-center-0.0.1-SNAPSHOT.jar .

# 可以根据需要暴露端口
# EXPOSE 8080

# 设置 Java 虚拟机参数
# ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 运行应用程序
CMD ["java", "-jar", "/app/target/user-center-backend-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]