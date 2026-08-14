# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.war app.war

# Set memory limits via environment variables for JVM inside container
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

EXPOSE 10000

# Pass JAVA_OPTS into the entrypoint command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]