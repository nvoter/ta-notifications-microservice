FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon help || true
COPY . .
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/build/libs/*.jar /app/app.jar
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]