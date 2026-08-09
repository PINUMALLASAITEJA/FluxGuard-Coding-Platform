FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM openjdk:21-jre-slim

WORKDIR /app

COPY --from=builder /app/target/coding-platform-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]