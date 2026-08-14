# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.war app.war

# Memory tuning for Render 512MB limit
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC"

EXPOSE 10000

# Extract WAR to allow Tomcat's JSP compiler (Jasper) to serve JSP views properly
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djdt.compiler.useSingleThread=true -jar app.war"]