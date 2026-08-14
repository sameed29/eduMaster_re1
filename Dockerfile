# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run (Uses JDK so Tomcat Jasper can compile JSP files)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.war app.war

# JVM limits for Render 512MB RAM tier
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]