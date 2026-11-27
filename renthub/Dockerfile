# Backend image (NFR-2): build the Spring Boot jar with the Gradle wrapper, run it on a JRE as a non-root user.
# Base images: eclipse-temurin (GPLv2 with Classpath Exception), free.
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /src
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon bootJar && cp build/libs/*.jar /src/app.jar

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S renthub && adduser -S renthub -G renthub
WORKDIR /app
COPY --from=build /src/app.jar app.jar
USER renthub
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "--enable-native-access=ALL-UNNAMED", "-jar", "/app/app.jar"]
