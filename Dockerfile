# --- Run Stage ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", \
    "-Xmx256m", \
    "-Xms128m", \
    "-XX:MaxMetaspaceSize=160m", \
    "-XX:+UseSerialGC", \
    "-Dspring.devtools.restart.enabled=false", \
    "-jar", "app.jar"]