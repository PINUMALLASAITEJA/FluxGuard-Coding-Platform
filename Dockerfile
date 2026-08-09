FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/coding-platform-0.0.1-SNAPSHOT.jar"]