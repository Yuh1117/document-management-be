FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon -q

CMD ["./gradlew", "bootRun", "--no-daemon"]
